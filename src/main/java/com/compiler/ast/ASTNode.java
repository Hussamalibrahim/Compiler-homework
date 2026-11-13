package com.compiler.ast;

import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ASTNode {
    private final String nodeName;
    private final int lineNumber;
    private final String sourceFile;
    private final List<ASTNode> children = new ArrayList<>();

    public ASTNode(String nodeName, int lineNumber, String sourceFile) {
        this.nodeName = nodeName;
        this.lineNumber = lineNumber;
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public ASTNode(String nodeName, int lineNumber) {
        this(nodeName, lineNumber, "");
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void addChild(ASTNode child) {
        if (child != null) {
            children.add(child);
        }
    }

    public void addChildren(List<? extends ASTNode> newChildren) {
        if (newChildren != null) {
            children.addAll(newChildren);
        }
    }

    public List<ASTNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public abstract void accept(ASTVisitor visitor);

    @Override
    public String toString() {
        return String.format("%s (line %d)", nodeName, lineNumber);
    }
}
