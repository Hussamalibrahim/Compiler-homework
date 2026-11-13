package com.compiler.ast.css;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class CssRuleNode extends ASTNode {
    private final CssSelectorNode selector;
    private final List<CssDeclarationNode> declarations = new ArrayList<>();

    public CssRuleNode(CssSelectorNode selector, int lineNumber, String sourceFile) {
        super("CssRuleNode: " + (selector != null ? selector.getSelector() : ""), lineNumber, sourceFile);
        this.selector = selector;
        if (selector != null) {
            addChild(selector);
        }
    }

    public CssSelectorNode getSelector() {
        return selector;
    }

    public void addDeclaration(CssDeclarationNode declaration) {
        if (declaration != null) {
            declarations.add(declaration);
            addChild(declaration);
        }
    }

    public List<CssDeclarationNode> getDeclarations() {
        return declarations;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
