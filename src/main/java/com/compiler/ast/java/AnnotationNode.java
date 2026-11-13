package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import lombok.Getter;

@Getter
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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
