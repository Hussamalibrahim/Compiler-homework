package com.compiler.ast.html;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class HtmlElementNode extends ASTNode {
    private final String tagName;
    private final boolean selfClosing;
    private final List<HtmlAttributeNode> standardAttributes = new ArrayList<>();
    private final List<ThymeleafAttributeNode> thymeleafAttributes = new ArrayList<>();

    public HtmlElementNode(String tagName, boolean selfClosing, int lineNumber, String sourceFile) {
        super("HtmlElementNode: <" + tagName + ">", lineNumber, sourceFile);
        this.tagName = tagName;
        this.selfClosing = selfClosing;
    }

    public String getTagName() {
        return tagName;
    }

    public boolean isSelfClosing() {
        return selfClosing;
    }

    public void addStandardAttribute(HtmlAttributeNode attr) {
        if (attr != null) {
            standardAttributes.add(attr);
            addChild(attr);
        }
    }

    public void addThymeleafAttribute(ThymeleafAttributeNode attr) {
        if (attr != null) {
            thymeleafAttributes.add(attr);
            addChild(attr);
        }
    }

    public List<HtmlAttributeNode> getStandardAttributes() {
        return standardAttributes;
    }

    public List<ThymeleafAttributeNode> getThymeleafAttributes() {
        return thymeleafAttributes;
    }

    public ThymeleafAttributeNode getThymeleafAttribute(String directive) {
        for (ThymeleafAttributeNode node : thymeleafAttributes) {
            if (node.getDirective().equalsIgnoreCase(directive)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
