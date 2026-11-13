package com.compiler.parser;

public class ParserException extends Exception {
    private final int line;
    private final int column;
    private final String sourceFile;

    public ParserException(String message, int line, int column, String sourceFile) {
        super(String.format("Syntax Error [%s:%d:%d]: %s", sourceFile, line, column, message));
        this.line = line;
        this.column = column;
        this.sourceFile = sourceFile;
    }

    public ParserException(String message, int line, int column) {
        this(message, line, column, "");
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getSourceFile() {
        return sourceFile;
    }
}
