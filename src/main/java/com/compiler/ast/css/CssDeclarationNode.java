package com.compiler.ast.css;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class CssDeclarationNode extends ASTNode {
    private final String property;
    private final String value;

    public CssDeclarationNode(String property, String value, int lineNumber, String sourceFile) {
        super("CssDeclarationNode: " + property + ": " + value, lineNumber, sourceFile);
        this.property = property;
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
