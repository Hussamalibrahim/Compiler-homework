package com.compiler.analyzer;

import com.compiler.ast.ASTNode;
import com.compiler.ast.ProjectNode;
import com.compiler.ast.html.HtmlElementNode;
import com.compiler.ast.html.TemplateNode;
import com.compiler.ast.html.ThymeleafAttributeNode;
import com.compiler.ast.html.ThymeleafExpressionNode;
import com.compiler.ast.java.ControllerNode;
import com.compiler.ast.java.MethodNode;
import com.compiler.ast.java.ModelAttributeNode;
import com.compiler.ast.java.ReturnStatementNode;

import java.util.*;

public class RelationshipAnalyzer {
    private final ProjectNode projectNode;
    private final Map<String, TemplateNode> templatesByName = new HashMap<>();

    public RelationshipAnalyzer(ProjectNode projectNode) {
        this.projectNode = projectNode;
    }

    public AnalysisReport analyze() {
        AnalysisReport report = new AnalysisReport();
        indexTemplates();

        // Find all controllers
        List<ControllerNode> controllers = new ArrayList<>();
        findControllers(projectNode, controllers);

        for (ControllerNode controller : controllers) {
            for (MethodNode method : controller.getMethods()) {
                analyzeMethod(controller, method, report);
            }
        }

        return report;
    }

    private void indexTemplates() {
        templatesByName.clear();
        List<TemplateNode> templates = new ArrayList<>();
        findTemplates(projectNode, templates);

        for (TemplateNode tmpl : templates) {
            String name = normalizeTemplateName(tmpl.getTemplateName());
            templatesByName.put(name, tmpl);

            String pathName = normalizeTemplateName(tmpl.getTemplatePath());
            templatesByName.put(pathName, tmpl);
        }
    }

    private void analyzeMethod(ControllerNode controller, MethodNode method, AnalysisReport report) {
        ReturnStatementNode ret = method.getReturnStatement();
        if (ret == null) return;

        String rawReturn = ret.getCleanTemplateName();
        if (rawReturn.isEmpty()) return;

        String httpMethod = method.getEndpoint() != null ? method.getEndpoint().getHttpMethod() : "GET";
        String endpointPath = method.getEndpoint() != null ? method.getEndpoint().getPath() : "/" + method.getMethodName();

        // Handle Redirect
        if (ret.isRedirect()) {
            AnalysisReport.RelationshipEntry entry = new AnalysisReport.RelationshipEntry(
                    controller.getClassName(), method.getMethodName(), httpMethod, endpointPath, rawReturn);
            entry.setStatus(true, "Redirect Route");
            report.addRelationship(entry);
            return;
        }

        String templateKey = normalizeTemplateName(rawReturn);
        TemplateNode templateNode = templatesByName.get(templateKey);

        AnalysisReport.RelationshipEntry entry = new AnalysisReport.RelationshipEntry(
                controller.getClassName(), method.getMethodName(), httpMethod, endpointPath, rawReturn);

        // Model attributes passed from this controller method
        Set<String> modelAttrNames = new LinkedHashSet<>();
        for (ModelAttributeNode attr : method.getModelAttributes()) {
            modelAttrNames.add(attr.getAttributeName());
            entry.addModelAttribute(attr.getAttributeName());
        }

        if (templateNode == null) {
            // Missing template error!
            entry.setStatus(false, "Template not found: " + rawReturn);
            report.addRelationship(entry);
            report.addDiagnostic(new Diagnostic(
                    Diagnostic.Severity.ERROR,
                    String.format("Missing template '%s' returned by controller method '%s.%s()'",
                            rawReturn, controller.getClassName(), method.getMethodName()),
                    ret.getSourceFile(),
                    ret.getLineNumber(),
                    "MISSING_TEMPLATE"
            ));
            return;
        }

        // Validate variables inside template
        Set<String> referencedModelAttrs = new HashSet<>();
        Deque<Set<String>> iterationScopes = new ArrayDeque<>();

        validateElementVariables(templateNode, modelAttrNames, iterationScopes, referencedModelAttrs, entry, report);

        // Check for unused model attributes
        for (String attr : modelAttrNames) {
            if (!referencedModelAttrs.contains(attr)) {
                report.addDiagnostic(new Diagnostic(
                        Diagnostic.Severity.WARNING,
                        String.format("Model attribute '%s' is provided by '%s.%s()' but never referenced in template '%s'",
                                attr, controller.getClassName(), method.getMethodName(), templateKey),
                        method.getSourceFile(),
                        method.getLineNumber(),
                        "UNUSED_MODEL_ATTRIBUTE"
                ));
            }
        }

        if (entry.getUnresolvedVariables().isEmpty()) {
            entry.setStatus(true, "Valid");
        } else {
            entry.setStatus(false, "Contains unresolved variables");
        }

        report.addRelationship(entry);
    }

    private void validateElementVariables(ASTNode node, Set<String> modelAttrs, Deque<Set<String>> iterationScopes,
                                          Set<String> referencedModelAttrs, AnalysisReport.RelationshipEntry entry, AnalysisReport report) {

        boolean pushedScope = false;

        if (node instanceof HtmlElementNode) {
            HtmlElementNode element = (HtmlElementNode) node;
            ThymeleafAttributeNode eachAttr = element.getThymeleafAttribute("th:each");

            if (eachAttr != null && eachAttr.getExpressionNode() != null) {
                ThymeleafExpressionNode expr = eachAttr.getExpressionNode();
                String iterVar = expr.getIterationVariable();
                String collectionVar = expr.getBaseVariable();

                // Check if collection variable is resolved
                if (isVariableInScope(collectionVar, modelAttrs, iterationScopes)) {
                    referencedModelAttrs.add(collectionVar);
                    entry.addResolvedVariable(collectionVar);
                } else if (!collectionVar.isEmpty()) {
                    entry.addUnresolvedVariable(collectionVar);
                    report.addDiagnostic(new Diagnostic(
                            Diagnostic.Severity.ERROR,
                            String.format("Unresolved collection variable '${%s}' in th:each", collectionVar),
                            expr.getSourceFile(),
                            expr.getLineNumber(),
                            "UNRESOLVED_VARIABLE"
                    ));
                }

                // Push new iteration scope
                Set<String> newScope = new HashSet<>();
                if (!iterVar.isEmpty()) {
                    newScope.add(iterVar);
                }
                iterationScopes.push(newScope);
                pushedScope = true;
            }

            // Check all other Thymeleaf attributes on this element
            for (ThymeleafAttributeNode thAttr : element.getThymeleafAttributes()) {
                if ("th:each".equalsIgnoreCase(thAttr.getDirective())) continue; // already handled
                ThymeleafExpressionNode expr = thAttr.getExpressionNode();
                if (expr != null) {
                    validateThymeleafExpression(expr, modelAttrs, iterationScopes, referencedModelAttrs, entry, report);
                }
            }
        }

        // Recurse children
        for (ASTNode child : node.getChildren()) {
            validateElementVariables(child, modelAttrs, iterationScopes, referencedModelAttrs, entry, report);
        }

        // Pop iteration scope
        if (pushedScope) {
            iterationScopes.pop();
        }
    }

    private void validateThymeleafExpression(ThymeleafExpressionNode expr, Set<String> modelAttrs,
                                            Deque<Set<String>> iterationScopes, Set<String> referencedModelAttrs,
                                            AnalysisReport.RelationshipEntry entry, AnalysisReport report) {
        String baseVar = expr.getBaseVariable();
        if (baseVar.isEmpty()) return;

        // Skip standard thymeleaf built-ins or link expressions
        if (expr.getKind() == ThymeleafExpressionNode.ExpressionKind.LINK ||
                baseVar.startsWith("#") || baseVar.equals("param") || baseVar.equals("session")) {
            return;
        }

        if (isVariableInScope(baseVar, modelAttrs, iterationScopes)) {
            if (modelAttrs.contains(baseVar)) {
                referencedModelAttrs.add(baseVar);
            }
            entry.addResolvedVariable(expr.getRawExpression());
        } else {
            entry.addUnresolvedVariable(baseVar);
            report.addDiagnostic(new Diagnostic(
                    Diagnostic.Severity.ERROR,
                    String.format("Unresolved variable '%s' in expression '%s'", baseVar, expr.getRawExpression()),
                    expr.getSourceFile(),
                    expr.getLineNumber(),
                    "UNRESOLVED_VARIABLE"
            ));
        }
    }

    private boolean isVariableInScope(String var, Set<String> modelAttrs, Deque<Set<String>> iterationScopes) {
        if (modelAttrs.contains(var)) return true;
        for (Set<String> scope : iterationScopes) {
            if (scope.contains(var)) return true;
        }
        return false;
    }

    private String normalizeTemplateName(String name) {
        if (name == null) return "";
        String s = name.replace('\\', '/').trim();
        while (s.startsWith("/")) s = s.substring(1);
        int templatesIdx = s.indexOf("templates/");
        if (templatesIdx != -1) {
            s = s.substring(templatesIdx + "templates/".length());
        }
        if (s.endsWith(".html")) {
            s = s.substring(0, s.length() - ".html".length());
        }
        return s;
    }

    private void findControllers(ASTNode node, List<ControllerNode> list) {
        if (node instanceof ControllerNode) {
            list.add((ControllerNode) node);
        }
        for (ASTNode child : node.getChildren()) {
            findControllers(child, list);
        }
    }

    private void findTemplates(ASTNode node, List<TemplateNode> list) {
        if (node instanceof TemplateNode) {
            list.add((TemplateNode) node);
        }
        for (ASTNode child : node.getChildren()) {
            findTemplates(child, list);
        }
    }
}
