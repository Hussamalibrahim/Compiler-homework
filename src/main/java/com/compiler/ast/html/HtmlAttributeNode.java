package com.compiler.ast.html;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class HtmlAttributeNode extends ASTNode {
    private final String attributeName;
    private final String attributeValue;

    public HtmlAttributeNode(String attributeName, String attributeValue, int lineNumber, String sourceFile) {
        super("HtmlAttributeNode: " + attributeName + "=\"" + attributeValue + "\"", lineNumber, sourceFile);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue != null ? attributeValue : "";
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
