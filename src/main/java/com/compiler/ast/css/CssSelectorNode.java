package com.compiler.ast.css;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class CssSelectorNode extends ASTNode {
    private final String selector;

    public CssSelectorNode(String selector, int lineNumber, String sourceFile) {
        super("CssSelectorNode: " + selector, lineNumber, sourceFile);
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
