package com.compiler.ast.java;

import com.compiler.visitor.ASTVisitor;

public class ControllerNode extends ClassNode {
    private final String basePath;
    private final boolean isRestController;

    public ControllerNode(String className, String packageName, String basePath, boolean isRestController, int lineNumber, String sourceFile) {
        super(className, packageName, lineNumber, sourceFile);
        this.basePath = basePath != null ? basePath : "";
        this.isRestController = isRestController;
    }

    public String getBasePath() {
        return basePath;
    }

    public boolean isRestController() {
        return isRestController;
    }

    @Override
    public String getNodeName() {
        return "ControllerNode: " + getClassName() + (basePath.isEmpty() ? "" : " (base: " + basePath + ")");
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
