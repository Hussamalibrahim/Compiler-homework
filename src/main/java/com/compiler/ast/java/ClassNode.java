package com.compiler.ast.java;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class ClassNode extends ASTNode {
    private final String className;
    private final String packageName;
    private final List<AnnotationNode> annotations = new ArrayList<>();
    private final List<MethodNode> methods = new ArrayList<>();
    private final List<VariableNode> fields = new ArrayList<>();

    public ClassNode(String className, String packageName, int lineNumber, String sourceFile) {
        super("ClassNode: " + className, lineNumber, sourceFile);
        this.className = className;
        this.packageName = packageName != null ? packageName : "";
    }

    public String getClassName() {
        return className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void addAnnotation(AnnotationNode annotation) {
        if (annotation != null) {
            annotations.add(annotation);
            addChild(annotation);
        }
    }

    public void addMethod(MethodNode method) {
        if (method != null) {
            methods.add(method);
            addChild(method);
        }
    }

    public void addField(VariableNode field) {
        if (field != null) {
            fields.add(field);
            addChild(field);
        }
    }

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    public List<MethodNode> getMethods() {
        return methods;
    }

    public List<VariableNode> getFields() {
        return fields;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
