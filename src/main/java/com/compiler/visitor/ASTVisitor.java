package com.compiler.visitor;

import com.compiler.ast.*;
import com.compiler.ast.java.*;
import com.compiler.ast.html.*;
import com.compiler.ast.css.*;

public interface ASTVisitor {
    void visit(ProjectNode node);

    // Java & Spring nodes
    void visit(ClassNode node);
    void visit(ControllerNode node);
    void visit(MethodNode node);
    void visit(ParameterNode node);
    void visit(VariableNode node);
    void visit(AnnotationNode node);
    void visit(EndpointNode node);
    void visit(ModelAttributeNode node);
    void visit(ReturnStatementNode node);

    // HTML & Thymeleaf nodes
    void visit(TemplateNode node);
    void visit(HtmlElementNode node);
    void visit(HtmlAttributeNode node);
    void visit(ThymeleafAttributeNode node);
    void visit(ThymeleafExpressionNode node);
    void visit(TextNode node);

    // CSS nodes
    void visit(StyleSheetNode node);
    void visit(CssRuleNode node);
    void visit(CssSelectorNode node);
    void visit(CssDeclarationNode node);
}
