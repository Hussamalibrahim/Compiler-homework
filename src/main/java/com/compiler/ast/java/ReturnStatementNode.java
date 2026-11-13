package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class ReturnStatementNode extends ASTNode {
    private final String returnedExpression;

    public ReturnStatementNode(String returnedExpression, int lineNumber, String sourceFile) {
        super("ReturnStatementNode: " + returnedExpression, lineNumber, sourceFile);
        this.returnedExpression = returnedExpression != null ? returnedExpression : "";
    }

    public String getReturnedExpression() {
        return returnedExpression;
    }

    public boolean isRedirect() {
        return returnedExpression.startsWith("redirect:");
    }

    public String getCleanTemplateName() {
        String clean = returnedExpression.trim();
        if (clean.startsWith("\"") && clean.endsWith("\"") && clean.length() >= 2) {
            clean = clean.substring(1, clean.length() - 1);
        }
        return clean;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
