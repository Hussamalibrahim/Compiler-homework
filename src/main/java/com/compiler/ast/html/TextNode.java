package com.compiler.ast.html;

import com.compiler.ast.ASTNode;
import com.compiler.visitor.ASTVisitor;

public class TextNode extends ASTNode {
    private final String content;

    public TextNode(String content, int lineNumber, String sourceFile) {
        super("TextNode: \"" + (content.length() > 30 ? content.substring(0, 27) + "..." : content) + "\"", lineNumber, sourceFile);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
