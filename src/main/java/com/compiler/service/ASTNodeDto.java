package com.compiler.service;

import com.compiler.ast.ASTNode;
import com.compiler.ast.ProjectNode;
import com.compiler.ast.css.CssDeclarationNode;
import com.compiler.ast.css.CssRuleNode;
import com.compiler.ast.css.CssSelectorNode;
import com.compiler.ast.css.StyleSheetNode;
import com.compiler.ast.html.*;
import com.compiler.ast.java.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ASTNodeDto {
    private final String nodeType;
    private final String nodeName;
    private final int lineNumber;
    private final String sourceFile;
    private final String category;
    private final List<ASTNodeDto> children = new ArrayList<>();

    public ASTNodeDto(String nodeType, String nodeName, int lineNumber, String sourceFile, String category) {
        this.nodeType = nodeType;
        this.nodeName = nodeName;
        this.lineNumber = lineNumber;
        this.sourceFile = sourceFile != null ? sourceFile : "";
        this.category = category;
    }

    public void addChild(ASTNodeDto child) {
        if (child != null) {
            children.add(child);
        }
    }

    public static ASTNodeDto fromASTNode(ASTNode node) {
        if (node == null) return null;

        String category = "OTHER";
        String nodeType = node.getClass().getSimpleName();

        if (node instanceof ProjectNode) {
            category = "PROJECT";
        } else if (node instanceof ControllerNode) {
            category = "CONTROLLER";
        } else if (node instanceof ClassNode) {
            category = "JAVA";
        } else if (node instanceof MethodNode) {
            category = "METHOD";
        } else if (node instanceof EndpointNode) {
            category = "ENDPOINT";
        } else if (node instanceof ModelAttributeNode) {
            category = "MODEL_ATTR";
        } else if (node instanceof TemplateNode) {
            category = "TEMPLATE";
        } else if (node instanceof HtmlElementNode) {
            category = "HTML_ELEMENT";
        } else if (node instanceof ThymeleafAttributeNode) {
            category = "THYMELEAF";
        } else if (node instanceof StyleSheetNode) {
            category = "CSS_SHEET";
        } else if (node instanceof CssRuleNode || node instanceof CssSelectorNode || node instanceof CssDeclarationNode) {
            category = "CSS";
        }

        ASTNodeDto dto = new ASTNodeDto(nodeType, node.getNodeName(), node.getLineNumber(), node.getSourceFile(), category);
        for (ASTNode child : node.getChildren()) {
            dto.addChild(fromASTNode(child));
        }
        return dto;
    }
}
