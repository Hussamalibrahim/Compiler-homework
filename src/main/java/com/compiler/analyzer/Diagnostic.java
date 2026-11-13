package com.compiler.analyzer;

public class Diagnostic {
    public enum Severity {
        ERROR,
        WARNING,
        INFO
    }

    private final Severity severity;
    private final String message;
    private final String sourceFile;
    private final int lineNumber;
    private final String category;

    public Diagnostic(Severity severity, String message, String sourceFile, int lineNumber, String category) {
        this.severity = severity;
        this.message = message;
        this.sourceFile = sourceFile != null ? sourceFile : "";
        this.lineNumber = lineNumber;
        this.category = category != null ? category : "GENERAL";
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getMessage() {
        return message;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (file: %s, line %d) [%s]",
                severity, message, sourceFile, lineNumber, category);
    }
}
