package com.compiler.symboltable;

public class Symbol {
    private final String name;
    private final SymbolType type;
    private final String scopeName;
    private final String sourceFile;
    private final int lineNumber;
    private final String extraDetails;

    public Symbol(String name, SymbolType type, String scopeName, String sourceFile, int lineNumber, String extraDetails) {
        this.name = name;
        this.type = type;
        this.scopeName = scopeName != null ? scopeName : "global";
        this.sourceFile = sourceFile != null ? sourceFile : "";
        this.lineNumber = lineNumber;
        this.extraDetails = extraDetails != null ? extraDetails : "";
    }

    public Symbol(String name, SymbolType type, String scopeName, String sourceFile, int lineNumber) {
        this(name, type, scopeName, sourceFile, lineNumber, "");
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public String getScopeName() {
        return scopeName;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getExtraDetails() {
        return extraDetails;
    }

    @Override
    public String toString() {
        return String.format("%-22s | %-18s | %-16s | %-25s | Line %-4d | %s",
                name, type, scopeName, sourceFile, lineNumber, extraDetails);
    }
}
