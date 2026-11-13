package com.compiler.visitor;

import com.compiler.ast.ASTNode;
import com.compiler.ast.ProjectNode;
import com.compiler.ast.java.*;
import com.compiler.ast.html.*;
import com.compiler.ast.css.*;
import com.compiler.symboltable.Symbol;
import com.compiler.symboltable.SymbolTable;
import com.compiler.symboltable.SymbolType;

public class SymbolTableVisitor implements ASTVisitor {
    private final SymbolTable symbolTable;

    public SymbolTableVisitor(SymbolTable symbolTable) {
        this.symbolTable = symbolTable != null ? symbolTable : new SymbolTable();
    }

    public SymbolTableVisitor() {
        this(new SymbolTable());
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    @Override
    public void visit(ProjectNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ClassNode node) {
        symbolTable.addSymbol(node.getClassName(), SymbolType.CLASS, node.getSourceFile(), node.getLineNumber(), "package " + node.getPackageName());
        symbolTable.enterScope("class:" + node.getClassName());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        symbolTable.exitScope();
    }

    @Override
    public void visit(ControllerNode node) {
        String details = (node.isRestController() ? "@RestController" : "@Controller") +
                (node.getBasePath().isEmpty() ? "" : " base: " + node.getBasePath());
        symbolTable.addSymbol(node.getClassName(), SymbolType.CONTROLLER, node.getSourceFile(), node.getLineNumber(), details);
        symbolTable.enterScope("controller:" + node.getClassName());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        symbolTable.exitScope();
    }

    @Override
    public void visit(MethodNode node) {
        symbolTable.addSymbol(node.getMethodName(), SymbolType.METHOD, node.getSourceFile(), node.getLineNumber(), "returns " + node.getReturnType());
        symbolTable.enterScope("method:" + node.getMethodName());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        symbolTable.exitScope();
    }

    @Override
    public void visit(ParameterNode node) {
        symbolTable.addSymbol(node.getName(), SymbolType.PARAMETER, node.getSourceFile(), node.getLineNumber(), "type: " + node.getType());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(VariableNode node) {
        symbolTable.addSymbol(node.getName(), SymbolType.VARIABLE, node.getSourceFile(), node.getLineNumber(), "type: " + node.getType());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(AnnotationNode node) {
        symbolTable.addSymbol(node.getAnnotationName(), SymbolType.ANNOTATION, node.getSourceFile(), node.getLineNumber(), node.getValue());
    }

    @Override
    public void visit(EndpointNode node) {
        symbolTable.addSymbol(node.getPath(), SymbolType.ENDPOINT, node.getSourceFile(), node.getLineNumber(), "HTTP " + node.getHttpMethod());
    }

    @Override
    public void visit(ModelAttributeNode node) {
        symbolTable.addSymbol(node.getAttributeName(), SymbolType.MODEL_ATTRIBUTE, node.getSourceFile(), node.getLineNumber(), "expr: " + node.getExpressionValue());
    }

    @Override
    public void visit(ReturnStatementNode node) {
        // Return statements don't create new symbols, but contribute to AST and relationships
    }

    @Override
    public void visit(TemplateNode node) {
        symbolTable.addSymbol(node.getTemplateName(), SymbolType.TEMPLATE, node.getSourceFile(), node.getLineNumber(), "path: " + node.getTemplatePath());
        symbolTable.enterScope("template:" + node.getTemplateName());
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
        symbolTable.exitScope();
    }

    @Override
    public void visit(HtmlElementNode node) {
        boolean isEachScope = false;
        ThymeleafAttributeNode eachAttr = node.getThymeleafAttribute("th:each");
        if (eachAttr != null && eachAttr.getExpressionNode() != null) {
            String iterVar = eachAttr.getExpressionNode().getIterationVariable();
            if (!iterVar.isEmpty()) {
                symbolTable.enterScope("each:" + iterVar);
                isEachScope = true;
                symbolTable.addSymbol(iterVar, SymbolType.ITERATION_VARIABLE, node.getSourceFile(), node.getLineNumber(), "iterates " + eachAttr.getExpressionNode().getBaseVariable());
            }
        }

        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }

        if (isEachScope) {
            symbolTable.exitScope();
        }
    }

    @Override
    public void visit(HtmlAttributeNode node) {
        // Standard attributes
    }

    @Override
    public void visit(ThymeleafAttributeNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ThymeleafExpressionNode node) {
        if (!node.getBaseVariable().isEmpty()) {
            String details = "kind: " + node.getKind() + (node.getPropertyPath().isEmpty() ? "" : ", prop: " + node.getPropertyPath());
            symbolTable.addSymbol(node.getBaseVariable(), SymbolType.TEMPLATE_VARIABLE, node.getSourceFile(), node.getLineNumber(), details);
        }
    }

    @Override
    public void visit(TextNode node) {
        // Plain text
    }

    @Override
    public void visit(StyleSheetNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(CssRuleNode node) {
        for (ASTNode child : node.getChildren()) {
            child.accept(this);
        }
    }

    @Override
    public void visit(CssSelectorNode node) {
        symbolTable.addSymbol(node.getSelector(), SymbolType.CSS_SELECTOR, node.getSourceFile(), node.getLineNumber(), "CSS Rule");
    }

    @Override
    public void visit(CssDeclarationNode node) {
        // Declaration
    }
}
