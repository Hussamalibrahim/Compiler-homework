package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class AnnotationNode extends ASTNode {
    private final String annotationName;
    private final String value;

    public AnnotationNode(String annotationName, String value, int lineNumber, String sourceFile) {
        super("AnnotationNode: " + annotationName + (value != null && !value.isEmpty() ? "(\"" + value + "\")" : ""), lineNumber, sourceFile);
        this.annotationName = annotationName;
        this.value = value != null ? value : "";
    }

    public AnnotationNode(String annotationName, int lineNumber, String sourceFile) {
        this(annotationName, "", lineNumber, sourceFile);
    }

    public String getAnnotationName() {
        return annotationName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
