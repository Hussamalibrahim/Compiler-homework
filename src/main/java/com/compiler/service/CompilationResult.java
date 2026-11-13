package com.compiler.service;

import com.compiler.analyzer.AnalysisReport;
import com.compiler.analyzer.Diagnostic;
import com.compiler.ast.ProjectNode;
import com.compiler.symboltable.Symbol;
import com.compiler.symboltable.SymbolTable;
import com.compiler.symboltable.SymbolType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getScanTimestamp() {
        return scanTimestamp;
    }

    public void setScanTimestamp(String scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public int getJavaFilesCount() {
        return javaFilesCount;
    }

    public void setJavaFilesCount(int javaFilesCount) {
        this.javaFilesCount = javaFilesCount;
    }

    public int getHtmlFilesCount() {
        return htmlFilesCount;
    }

    public void setHtmlFilesCount(int htmlFilesCount) {
        this.htmlFilesCount = htmlFilesCount;
    }

    public int getCssFilesCount() {
        return cssFilesCount;
    }

    public void setCssFilesCount(int cssFilesCount) {
        this.cssFilesCount = cssFilesCount;
    }

    public int getTotalFilesCount() {
        return totalFilesCount;
    }

    public void setTotalFilesCount(int totalFilesCount) {
        this.totalFilesCount = totalFilesCount;
    }

    public int getTotalSymbolsCount() {
        return totalSymbolsCount;
    }

    public void setTotalSymbolsCount(int totalSymbolsCount) {
        this.totalSymbolsCount = totalSymbolsCount;
    }

    public int getAstNodesCount() {
        return astNodesCount;
    }

    public void setAstNodesCount(int astNodesCount) {
        this.astNodesCount = astNodesCount;
    }

    public int getClassesCount() {
        return classesCount;
    }

    public void setClassesCount(int classesCount) {
        this.classesCount = classesCount;
    }

    public int getControllersCount() {
        return controllersCount;
    }

    public void setControllersCount(int controllersCount) {
        this.controllersCount = controllersCount;
    }

    public int getEndpointsCount() {
        return endpointsCount;
    }

    public void setEndpointsCount(int endpointsCount) {
        this.endpointsCount = endpointsCount;
    }

    public int getTemplatesCount() {
        return templatesCount;
    }

    public void setTemplatesCount(int templatesCount) {
        this.templatesCount = templatesCount;
    }

    public int getRelationshipsCount() {
        return relationshipsCount;
    }

    public void setRelationshipsCount(int relationshipsCount) {
        this.relationshipsCount = relationshipsCount;
    }

    public int getValidRelationshipsCount() {
        return validRelationshipsCount;
    }

    public void setValidRelationshipsCount(int validRelationshipsCount) {
        this.validRelationshipsCount = validRelationshipsCount;
    }

    public int getInvalidRelationshipsCount() {
        return invalidRelationshipsCount;
    }

    public void setInvalidRelationshipsCount(int invalidRelationshipsCount) {
        this.invalidRelationshipsCount = invalidRelationshipsCount;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public long getWarningCount() {
        return warningCount;
    }

    public void setWarningCount(long warningCount) {
        this.warningCount = warningCount;
    }

    public List<ProjectFileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<ProjectFileInfo> files) {
        this.files = files;
    }

    public ProjectNode getAstRoot() {
        return astRoot;
    }

    public void setAstRoot(ProjectNode astRoot) {
        this.astRoot = astRoot;
    }

    public ASTNodeDto getAstDto() {
        return astDto;
    }

    public void setAstDto(ASTNodeDto astDto) {
        this.astDto = astDto;
    }

    public String getAstText() {
        return astText;
    }

    public void setAstText(String astText) {
        this.astText = astText;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void setSymbolTable(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public void setSymbols(List<Symbol> symbols) {
        this.symbols = symbols;
    }

    public List<AnalysisReport.RelationshipEntry> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<AnalysisReport.RelationshipEntry> relationships) {
        this.relationships = relationships;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<Diagnostic> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
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
