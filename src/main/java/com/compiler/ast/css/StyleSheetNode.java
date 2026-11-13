package com.compiler.ast.css;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;

public class StyleSheetNode extends ASTNode {
    private final String sheetName;
    private final List<CssRuleNode> rules = new ArrayList<>();

    public StyleSheetNode(String sheetName, int lineNumber, String sourceFile) {
        super("StyleSheetNode: " + sheetName, lineNumber, sourceFile);
        this.sheetName = sheetName;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void addRule(CssRuleNode rule) {
        if (rule != null) {
            rules.add(rule);
            addChild(rule);
        }
    }

    public List<CssRuleNode> getRules() {
        return rules;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
