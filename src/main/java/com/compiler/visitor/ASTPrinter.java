package com.compiler.visitor;

import com.compiler.ast.ASTNode;
import com.compiler.ast.ProjectNode;
import com.compiler.ast.java.*;
import com.compiler.ast.html.*;
import com.compiler.ast.css.*;

import java.util.List;

public class ASTPrinter implements ASTVisitor {
    private final StringBuilder output = new StringBuilder();

    public String getOutput() {
        return output.toString();
    }

    public void print(ASTNode root) {
        output.setLength(0);
        if (root != null) {
            output.append(root.getNodeName());
            if (root.getLineNumber() > 0) {
                output.append(" [Line ").append(root.getLineNumber()).append("]");
            }
            if (root.getSourceFile() != null && !root.getSourceFile().isEmpty()) {
                output.append(" (").append(root.getSourceFile()).append(")");
            }
            output.append("\n");

            printChildren(root, "");
        }
    }

    private void printChildren(ASTNode parent, String prefix) {
        List<ASTNode> children = parent.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean isLast = (i == children.size() - 1);
            ASTNode child = children.get(i);

            output.append(prefix);
            output.append(isLast ? "\\-- " : "+-- ");
            output.append(child.getNodeName());
            if (child.getLineNumber() > 0) {
                output.append(" [Line ").append(child.getLineNumber()).append("]");
            }
            if (child.getSourceFile() != null && !child.getSourceFile().isEmpty()) {
                output.append(" (").append(child.getSourceFile()).append(")");
            }
            output.append("\n");

            String nextPrefix = prefix + (isLast ? "    " : "|   ");
            printChildren(child, nextPrefix);
        }
    }

    @Override
    public void visit(ProjectNode node) {
        print(node);
    }

    @Override
    public void visit(ClassNode node) {
        print(node);
    }

    @Override
    public void visit(ControllerNode node) {
        print(node);
    }

    @Override
    public void visit(MethodNode node) {
        print(node);
    }

    @Override
    public void visit(ParameterNode node) {
        print(node);
    }

    @Override
    public void visit(VariableNode node) {
        print(node);
    }

    @Override
    public void visit(AnnotationNode node) {
        print(node);
    }

    @Override
    public void visit(EndpointNode node) {
        print(node);
    }

    @Override
    public void visit(ModelAttributeNode node) {
        print(node);
    }

    @Override
    public void visit(ReturnStatementNode node) {
        print(node);
    }

    @Override
    public void visit(TemplateNode node) {
        print(node);
    }

    @Override
    public void visit(HtmlElementNode node) {
        print(node);
    }

    @Override
    public void visit(HtmlAttributeNode node) {
        print(node);
    }

    @Override
    public void visit(ThymeleafAttributeNode node) {
        print(node);
    }

    @Override
    public void visit(ThymeleafExpressionNode node) {
        print(node);
    }

    @Override
    public void visit(TextNode node) {
        print(node);
    }

    @Override
    public void visit(StyleSheetNode node) {
        print(node);
    }

    @Override
    public void visit(CssRuleNode node) {
        print(node);
    }

    @Override
    public void visit(CssSelectorNode node) {
        print(node);
    }

    @Override
    public void visit(CssDeclarationNode node) {
        print(node);
    }
}
