package com.compiler.ast.html;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class ThymeleafExpressionNode extends ASTNode {
    public enum ExpressionKind {
        VARIABLE,   // ${...}
        SELECTION,  // *{...}
        LINK,       // @{...}
        ITERATION,  // item : ${collection}
        LITERAL     // plain text
    }

    private final ExpressionKind kind;
    private final String rawExpression;
    private final String baseVariable;
    private final String propertyPath;
    private final String iterationVariable; // for th:each: the item name

    public ThymeleafExpressionNode(ExpressionKind kind, String rawExpression, String baseVariable, String propertyPath, String iterationVariable, int lineNumber, String sourceFile) {
        super("ThymeleafExpressionNode: [" + kind + "] " + rawExpression, lineNumber, sourceFile);
        this.kind = kind;
        this.rawExpression = rawExpression;
        this.baseVariable = baseVariable != null ? baseVariable : "";
        this.propertyPath = propertyPath != null ? propertyPath : "";
        this.iterationVariable = iterationVariable != null ? iterationVariable : "";
    }

    public ExpressionKind getKind() {
        return kind;
    }

    public String getRawExpression() {
        return rawExpression;
    }

    public String getBaseVariable() {
        return baseVariable;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public String getIterationVariable() {
        return iterationVariable;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
