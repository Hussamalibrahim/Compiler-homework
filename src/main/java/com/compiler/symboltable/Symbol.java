package com.compiler.symboltable;

import lombok.Getter;

@Getter
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

    @Override
    public String toString() {
        return String.format("%-22s | %-18s | %-16s | %-25s | Line %-4d | %s",
                name, type, scopeName, sourceFile, lineNumber, extraDetails);
    }
}
