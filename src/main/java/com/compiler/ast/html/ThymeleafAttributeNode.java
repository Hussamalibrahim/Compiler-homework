package com.compiler.ast.html;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class ThymeleafAttributeNode extends ASTNode {
    private final String directive;
    private final String rawValue;
    private ThymeleafExpressionNode expressionNode;

    public ThymeleafAttributeNode(String directive, String rawValue, int lineNumber, String sourceFile) {
        super("ThymeleafAttributeNode: " + directive + "=\"" + rawValue + "\"", lineNumber, sourceFile);
        this.directive = directive;
        this.rawValue = rawValue != null ? rawValue : "";
    }

    public String getDirective() {
        return directive;
    }

    public String getRawValue() {
        return rawValue;
    }

    public ThymeleafExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public void setExpressionNode(ThymeleafExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
        if (expressionNode != null) {
            addChild(expressionNode);
        }
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
