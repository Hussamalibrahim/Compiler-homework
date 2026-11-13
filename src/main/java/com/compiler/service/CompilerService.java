package com.compiler.service;

import com.compiler.analyzer.AnalysisReport;
import com.compiler.analyzer.Diagnostic;
import com.compiler.analyzer.RelationshipAnalyzer;
import com.compiler.ast.ASTNode;
import com.compiler.ast.ProjectNode;
import com.compiler.ast.css.StyleSheetNode;
import com.compiler.ast.html.TemplateNode;
import com.compiler.ast.java.ClassNode;
import com.compiler.ast.java.ControllerNode;
import com.compiler.ast.java.EndpointNode;
import com.compiler.lexer.CssLexer;
import com.compiler.lexer.HtmlLexer;
import com.compiler.lexer.JavaLexer;
import com.compiler.parser.CssParser;
import com.compiler.parser.HtmlThymeleafParser;
import com.compiler.parser.JavaParser;
import com.compiler.parser.ParserException;
import com.compiler.symboltable.SymbolTable;
import com.compiler.visitor.ASTPrinter;
import com.compiler.visitor.SymbolTableVisitor;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.stream.Stream;

@Service
public class CompilerService {

    private CompilationResult lastResult;
    private final Object lock = new Object();

    @PostConstruct
    public void init() {
        // Attempt to load the default sample project on startup
        try {
            Path defaultPath = resolveProjectPath("test-project");
            if (Files.exists(defaultPath)) {
                compileProject(defaultPath);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not automatically compile default test-project on startup: " + e.getMessage());
        }
    }

    public CompilationResult getLastResult() {
        synchronized (lock) {
            if (lastResult == null) {
                Path defaultPath = resolveProjectPath("test-project");
                if (Files.exists(defaultPath)) {
                    return compileProject(defaultPath);
                } else {
                    CompilationResult empty = new CompilationResult();
                    empty.setStatusMessage("No project analyzed yet. Please select a directory or upload a project.");
                    return empty;
                }
            }
            return lastResult;
        }
    }

    public CompilationResult compilePath(String directoryPath) {
        Path resolved = resolveProjectPath(directoryPath);
        if (!Files.exists(resolved) || !Files.isDirectory(resolved)) {
            CompilationResult errorResult = new CompilationResult();
            errorResult.setProjectName(directoryPath);
            errorResult.setProjectPath(directoryPath);
            errorResult.setSuccess(false);
            errorResult.setStatusMessage("Directory not found or is not accessible: " + directoryPath);
            errorResult.getDiagnostics().add(new Diagnostic(
                    Diagnostic.Severity.ERROR,
                    "Directory does not exist or cannot be accessed: " + directoryPath,
                    directoryPath,
                    0,
                    "IO_ERROR"
            ));
            errorResult.setErrorCount(1);
            synchronized (lock) {
                this.lastResult = errorResult;
            }
            return errorResult;
        }
        return compileProject(resolved);
    }

    public CompilationResult compileUploadedZip(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or missing.");
        }

        Path tempDir = Files.createTempDirectory("compiler_upload_" + System.currentTimeMillis());
        try (ZipInputStream zis = new ZipInputStream(multipartFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // Protect against Zip Slip vulnerability
                Path targetPath = tempDir.resolve(entry.getName()).normalize();
                if (!targetPath.startsWith(tempDir)) {
                    throw new SecurityException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    if (targetPath.getParent() != null) {
                        Files.createDirectories(targetPath.getParent());
                    }
                    Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }

        // Find the effective project root (if zipped folder has a single top-level folder)
        Path effectiveRoot = tempDir;
        try (Stream<Path> stream = Files.list(tempDir)) {
            List<Path> rootItems = stream.toList();
            if (rootItems.size() == 1 && Files.isDirectory(rootItems.get(0))) {
                effectiveRoot = rootItems.get(0);
            }
        }

        CompilationResult result = compileProject(effectiveRoot);
        result.setProjectName(multipartFile.getOriginalFilename());
        return result;
    }

    public CompilationResult compileProject(Path projectPath) {
        long startTime = System.currentTimeMillis();
        CompilationResult result = new CompilationResult();

        Path absPath = projectPath.toAbsolutePath().normalize();
        result.setProjectPath(absPath.toString());
        result.setProjectName(projectPath.getFileName() != null ? projectPath.getFileName().toString() : "Project");

        ProjectNode projectNode = new ProjectNode(result.getProjectName(), 1);
        AnalysisReport report = new AnalysisReport();

        List<Path> javaPaths = new ArrayList<>();
        List<Path> htmlPaths = new ArrayList<>();
        List<Path> cssPaths = new ArrayList<>();
        List<ProjectFileInfo> fileInfos = new ArrayList<>();

        if (Files.exists(absPath)) {
            try (Stream<Path> stream = Files.walk(absPath)) {
                stream.filter(Files::isRegularFile).forEach(p -> {
                    String lower = p.toString().toLowerCase();
                    String rel = absPath.relativize(p).toString().replace('\\', '/');
                    long size = 0;
                    int lines = 0;
                    try {
                        size = Files.size(p);
                        lines = (int) Files.lines(p).count();
                    } catch (Exception ignored) {
                    }

                    if (lower.endsWith(".java")) {
                        javaPaths.add(p);
                        fileInfos.add(new ProjectFileInfo(p.getFileName().toString(), rel, "JAVA", size, lines));
                    } else if (lower.endsWith(".html")) {
                        htmlPaths.add(p);
                        fileInfos.add(new ProjectFileInfo(p.getFileName().toString(), rel, "HTML", size, lines));
                    } else if (lower.endsWith(".css")) {
                        cssPaths.add(p);
                        fileInfos.add(new ProjectFileInfo(p.getFileName().toString(), rel, "CSS", size, lines));
                    }
                });
            } catch (IOException e) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "I/O error during project scanning: " + e.getMessage(),
                        absPath.toString(),
                        0,
                        "IO_ERROR"
                ));
            }
        }

        result.setFiles(fileInfos);
        result.setJavaFilesCount(javaPaths.size());
        result.setHtmlFilesCount(htmlPaths.size());
        result.setCssFilesCount(cssPaths.size());
        result.setTotalFilesCount(fileInfos.size());

        // 1. Parse Java files
        for (Path p : javaPaths) {
            String fileName = p.getFileName().toString();
            try {
                String content = Files.readString(p);
                JavaLexer lexer = new JavaLexer(content, fileName);
                JavaParser parser = new JavaParser(lexer.tokenize(), fileName);
                ClassNode classNode = parser.parse();
                projectNode.addChild(classNode);
            } catch (ParserException pe) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "Java Syntax Error: " + pe.getMessage(),
                        pe.getSourceFile() != null && !pe.getSourceFile().isEmpty() ? pe.getSourceFile() : fileName,
                        pe.getLine(),
                        "SYNTAX_ERROR"
                ));
            } catch (Exception e) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "Java Parsing Error: " + e.getMessage(),
                        fileName,
                        1,
                        "SYNTAX_ERROR"
                ));
            }
        }

        // 2. Parse HTML / Thymeleaf files
        for (Path p : htmlPaths) {
            String fileName = p.getFileName().toString();
            String relPath = absPath.relativize(p).toString().replace('\\', '/');
            String logicalName = relPath;
            int tmplIdx = relPath.indexOf("templates/");
            if (tmplIdx != -1) {
                logicalName = relPath.substring(tmplIdx + "templates/".length());
            }

            try {
                String content = Files.readString(p);
                HtmlLexer lexer = new HtmlLexer(content, fileName);
                HtmlThymeleafParser parser = new HtmlThymeleafParser(lexer.tokenize(), relPath, logicalName, fileName);
                TemplateNode templateNode = parser.parse();
                projectNode.addChild(templateNode);
            } catch (ParserException pe) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "HTML Syntax Error: " + pe.getMessage(),
                        pe.getSourceFile() != null && !pe.getSourceFile().isEmpty() ? pe.getSourceFile() : fileName,
                        pe.getLine(),
                        "SYNTAX_ERROR"
                ));
            } catch (Exception e) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "HTML Parsing Error: " + e.getMessage(),
                        fileName,
                        1,
                        "SYNTAX_ERROR"
                ));
            }
        }

        // 3. Parse CSS files
        for (Path p : cssPaths) {
            String fileName = p.getFileName().toString();
            try {
                String content = Files.readString(p);
                CssLexer lexer = new CssLexer(content, fileName);
                CssParser parser = new CssParser(lexer.tokenize(), fileName, fileName);
                StyleSheetNode sheetNode = parser.parse();
                projectNode.addChild(sheetNode);
            } catch (ParserException pe) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "CSS Syntax Error: " + pe.getMessage(),
                        pe.getSourceFile() != null && !pe.getSourceFile().isEmpty() ? pe.getSourceFile() : fileName,
                        pe.getLine(),
                        "SYNTAX_ERROR"
                ));
            } catch (Exception e) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.ERROR,
                        "CSS Parsing Error: " + e.getMessage(),
                        fileName,
                        1,
                        "SYNTAX_ERROR"
                ));
            }
        }

        // 4. Generate AST output
        ASTPrinter printer = new ASTPrinter();
        projectNode.accept(printer);
        String astOutput = printer.getOutput();

        // 5. Build Symbol Table
        SymbolTable symbolTable = new SymbolTable();
        SymbolTableVisitor symbolVisitor = new SymbolTableVisitor(symbolTable);
        projectNode.accept(symbolVisitor);

        // 6. Semantic & Controller-Template Relationship Analysis
        RelationshipAnalyzer analyzer = new RelationshipAnalyzer(projectNode);
        AnalysisReport semanticReport = analyzer.analyze();

        // Combine diagnostics
        for (Diagnostic d : semanticReport.getDiagnostics()) {
            report.addDiagnostic(d);
        }

        // Tally statistics
        int totalAstNodes = countNodes(projectNode);
        int classesCount = 0;
        int controllersCount = 0;
        int endpointsCount = 0;
        int templatesCount = 0;

        for (ASTNode child : projectNode.getChildren()) {
            if (child instanceof ControllerNode) {
                controllersCount++;
                classesCount++;
                endpointsCount += countEndpoints(child);
            } else if (child instanceof ClassNode) {
                classesCount++;
            } else if (child instanceof TemplateNode) {
                templatesCount++;
            }
        }

        int validRels = 0;
        int invalidRels = 0;
        for (AnalysisReport.RelationshipEntry rel : semanticReport.getRelationships()) {
            if (rel.isValid()) {
                validRels++;
            } else {
                invalidRels++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;

        result.setExecutionTimeMs(elapsed);
        result.setAstRoot(projectNode);
        result.setAstDto(ASTNodeDto.fromASTNode(projectNode));
        result.setAstText(astOutput);
        result.setAstNodesCount(totalAstNodes);
        result.setClassesCount(classesCount);
        result.setControllersCount(controllersCount);
        result.setEndpointsCount(endpointsCount);
        result.setTemplatesCount(templatesCount);

        result.setSymbolTable(symbolTable);
        result.setSymbols(symbolTable.getAllSymbols());
        result.setTotalSymbolsCount(symbolTable.getAllSymbols().size());

        result.setRelationships(semanticReport.getRelationships());
        result.setRelationshipsCount(semanticReport.getRelationships().size());
        result.setValidRelationshipsCount(validRels);
        result.setInvalidRelationshipsCount(invalidRels);

        result.setDiagnostics(report.getDiagnostics());
        result.setErrorCount(report.getErrorCount());
        result.setWarningCount(report.getWarningCount());

        result.setSuccess(report.getErrorCount() == 0);
        result.setStatusMessage(report.getErrorCount() == 0
                ? "Compilation and analysis completed with zero errors."
                : String.format("Completed with %d error(s) and %d warning(s).", report.getErrorCount(), report.getWarningCount()));

        synchronized (lock) {
            this.lastResult = result;
        }

        return result;
    }

    private int countNodes(ASTNode node) {
        if (node == null) return 0;
        int count = 1;
        for (ASTNode child : node.getChildren()) {
            count += countNodes(child);
        }
        return count;
    }

    private int countEndpoints(ASTNode node) {
        int count = 0;
        if (node instanceof EndpointNode) {
            count++;
        }
        for (ASTNode child : node.getChildren()) {
            count += countEndpoints(child);
        }
        return count;
    }

    private Path resolveProjectPath(String pathStr) {
        if (pathStr == null || pathStr.trim().isEmpty()) {
            pathStr = "test-project";
        }

        Path p = Paths.get(pathStr);
        if (Files.exists(p)) {
            return p;
        }

        Path userDir = Paths.get(System.getProperty("user.dir", "."));
        Path relativePath = userDir.resolve(pathStr);
        if (Files.exists(relativePath)) {
            return relativePath;
        }

        Path alt = Paths.get("src/main/resources").resolve(pathStr);
        if (Files.exists(alt)) {
            return alt;
        }

        return p;
    }
}
