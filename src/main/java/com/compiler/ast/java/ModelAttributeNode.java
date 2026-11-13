package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class ModelAttributeNode extends ASTNode {
    private final String attributeName;
    private final String expressionValue;

    public ModelAttributeNode(String attributeName, String expressionValue, int lineNumber, String sourceFile) {
        super("ModelAttributeNode: " + attributeName + " -> " + expressionValue, lineNumber, sourceFile);
        this.attributeName = attributeName;
        this.expressionValue = expressionValue != null ? expressionValue : "";
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getExpressionValue() {
        return expressionValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
