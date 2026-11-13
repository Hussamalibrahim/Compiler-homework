package com.compiler.ast;

import com.compiler.visitor.ASTVisitor;
import lombok.Getter;

@Getter
public class ProjectNode extends ASTNode {
    private final String projectName;

    public ProjectNode(String projectName, int lineNumber) {
        super("ProjectNode: " + projectName, lineNumber);
        this.projectName = projectName;
    }

    public ProjectNode(String projectName) {
        this(projectName, 1);
    }


    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
