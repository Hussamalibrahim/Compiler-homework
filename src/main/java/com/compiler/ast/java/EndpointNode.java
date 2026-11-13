package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class EndpointNode extends ASTNode {
    private final String httpMethod;
    private final String path;

    public EndpointNode(String httpMethod, String path, int lineNumber, String sourceFile) {
        super("EndpointNode: " + httpMethod + " " + path, lineNumber, sourceFile);
        this.httpMethod = httpMethod;
        this.path = path != null ? path : "";
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
