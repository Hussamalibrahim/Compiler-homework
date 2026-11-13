package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class VariableNode extends ASTNode {
    private final String type;
    private final String name;
    private final String initialValue;

    public VariableNode(String type, String name, String initialValue, int lineNumber, String sourceFile) {
        super("VariableNode: " + type + " " + name + (initialValue != null && !initialValue.isEmpty() ? " = " + initialValue : ""), lineNumber, sourceFile);
        this.type = type;
        this.name = name;
        this.initialValue = initialValue != null ? initialValue : "";
    }

    public VariableNode(String type, String name, int lineNumber, String sourceFile) {
        this(type, name, "", lineNumber, sourceFile);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getInitialValue() {
        return initialValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
