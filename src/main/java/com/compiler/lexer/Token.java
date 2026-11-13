package com.compiler.lexer;

public class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;
    private final String sourceFile;

    public Token(TokenType type, String value, int line, int column, String sourceFile) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
        this.sourceFile = sourceFile;
    }

    public Token(TokenType type, String value, int line, int column) {
        this(type, value, line, column, "");
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
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

    @Override
    public String toString() {
        return String.format("Token[%s, '%s', line %d:%d]", type, value, line, column);
    }
}
