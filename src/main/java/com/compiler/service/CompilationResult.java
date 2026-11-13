package com.compiler.service;

import com.compiler.analyzer.AnalysisReport;
import com.compiler.analyzer.Diagnostic;
import com.compiler.ast.ProjectNode;
import com.compiler.symboltable.Symbol;
import com.compiler.symboltable.SymbolTable;
import com.compiler.symboltable.SymbolType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class CompilationResult {
    private String projectName = "Unnamed";
    private String projectPath = "";
    private String scanTimestamp;
    private long executionTimeMs;

    private int javaFilesCount;
    private int htmlFilesCount;
    private int cssFilesCount;
    private int totalFilesCount;

    private int totalSymbolsCount;
    private int astNodesCount;
    private int classesCount;
    private int controllersCount;
    private int endpointsCount;
    private int templatesCount;

    private int relationshipsCount;
    private int validRelationshipsCount;
    private int invalidRelationshipsCount;

    private long errorCount;
    private long warningCount;

    private List<ProjectFileInfo> files = new ArrayList<>();
    private ProjectNode astRoot;
    private ASTNodeDto astDto;
    private String astText = "";

    private SymbolTable symbolTable;
    private List<Symbol> symbols = new ArrayList<>();
    private List<AnalysisReport.RelationshipEntry> relationships = new ArrayList<>();
    private List<Diagnostic> diagnostics = new ArrayList<>();

    private boolean success = true;
    private String statusMessage = "Compiled successfully";

    public CompilationResult() {
        this.scanTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public Set<String> getSymbolTypes() {
        return symbols.stream()
                .map(s -> s.getType().name())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Set<String> getSymbolScopes() {
        return symbols.stream()
                .map(Symbol::getScopeName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Map<String, Long> getSymbolCountsByType() {
        return symbols.stream()
                .collect(Collectors.groupingBy(s -> s.getType().name(), Collectors.counting()));
    }

    public List<Diagnostic> getErrorDiagnostics() {
        return diagnostics.stream()
                .filter(d -> d.getSeverity() == Diagnostic.Severity.ERROR)
                .collect(Collectors.toList());
    }

    public List<Diagnostic> getWarningDiagnostics() {
        return diagnostics.stream()
                .filter(d -> d.getSeverity() == Diagnostic.Severity.WARNING)
                .collect(Collectors.toList());
    }
}
