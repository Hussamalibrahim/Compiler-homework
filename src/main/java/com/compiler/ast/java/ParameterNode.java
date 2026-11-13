package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class ParameterNode extends ASTNode {
    private final String type;
    private final String name;
    private final List<AnnotationNode> annotations = new ArrayList<>();

    public ParameterNode(String type, String name, int lineNumber, String sourceFile) {
        super("ParameterNode: " + type + " " + name, lineNumber, sourceFile);
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void addAnnotation(AnnotationNode annotation) {
        if (annotation != null) {
            annotations.add(annotation);
            addChild(annotation);
        }
    }

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
