package com.compiler.ast.html;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class TemplateNode extends ASTNode {
    private final String templatePath;
    private final String templateName;
    private final List<HtmlElementNode> rootElements = new ArrayList<>();

    public TemplateNode(String templatePath, String templateName, int lineNumber, String sourceFile) {
        super("TemplateNode: " + templateName, lineNumber, sourceFile);
        this.templatePath = templatePath;
        this.templateName = templateName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void addRootElement(HtmlElementNode element) {
        if (element != null) {
            rootElements.add(element);
            addChild(element);
        }
    }

    public List<HtmlElementNode> getRootElements() {
        return rootElements;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
