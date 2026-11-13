package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class MethodNode extends ASTNode {
    private final String methodName;
    private final String returnType;
    private final List<AnnotationNode> annotations = new ArrayList<>();
    private final List<ParameterNode> parameters = new ArrayList<>();
    private final List<ModelAttributeNode> modelAttributes = new ArrayList<>();
    private ReturnStatementNode returnStatement;
    private EndpointNode endpoint;

    public MethodNode(String methodName, String returnType, int lineNumber, String sourceFile) {
        super("MethodNode: " + returnType + " " + methodName, lineNumber, sourceFile);
        this.methodName = methodName;
        this.returnType = returnType != null ? returnType : "void";
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public void addAnnotation(AnnotationNode annotation) {
        if (annotation != null) {
            annotations.add(annotation);
            addChild(annotation);
        }
    }

    public void addParameter(ParameterNode param) {
        if (param != null) {
            parameters.add(param);
            addChild(param);
        }
    }

    public void setEndpoint(EndpointNode endpoint) {
        this.endpoint = endpoint;
        if (endpoint != null) {
            addChild(endpoint);
        }
    }

    public void addModelAttribute(ModelAttributeNode modelAttr) {
        if (modelAttr != null) {
            modelAttributes.add(modelAttr);
            addChild(modelAttr);
        }
    }

    public void setReturnStatement(ReturnStatementNode returnStatement) {
        this.returnStatement = returnStatement;
        if (returnStatement != null) {
            addChild(returnStatement);
        }
    }

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public List<ModelAttributeNode> getModelAttributes() {
        return modelAttributes;
    }

    public ReturnStatementNode getReturnStatement() {
        return returnStatement;
    }

    public EndpointNode getEndpoint() {
        return endpoint;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
