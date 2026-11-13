package com.compiler.analyzer;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AnalysisReport {
    @Getter
    public static class RelationshipEntry {
        private final String controllerName;
        private final String methodName;
        private final String httpMethod;
        private final String endpointPath;
        private final String returnedTemplate;
        private final List<String> modelAttributes = new ArrayList<>();
        private final List<String> resolvedVariables = new ArrayList<>();
        private final List<String> unresolvedVariables = new ArrayList<>();
        private boolean valid = true;
        private String statusMessage = "Valid";

        public RelationshipEntry(String controllerName, String methodName, String httpMethod, String endpointPath, String returnedTemplate) {
            this.controllerName = controllerName;
            this.methodName = methodName;
            this.httpMethod = httpMethod;
            this.endpointPath = endpointPath;
            this.returnedTemplate = returnedTemplate;
        }

        public void addModelAttribute(String attr) {
            modelAttributes.add(attr);
        }

        public void addResolvedVariable(String var) {
            resolvedVariables.add(var);
        }

        public void addUnresolvedVariable(String var) {
            unresolvedVariables.add(var);
            valid = false;
        }

        public void setStatus(boolean valid, String statusMessage) {
            this.valid = valid;
            this.statusMessage = statusMessage;
        }

    }

    private final List<RelationshipEntry> relationships = new ArrayList<>();
    private final List<Diagnostic> diagnostics = new ArrayList<>();

    public void addRelationship(RelationshipEntry entry) {
        relationships.add(entry);
    }

    public void addDiagnostic(Diagnostic diag) {
        diagnostics.add(diag);
    }

    public long getErrorCount() {
        return diagnostics.stream().filter(d -> d.getSeverity() == Diagnostic.Severity.ERROR).count();
    }

    public long getWarningCount() {
        return diagnostics.stream().filter(d -> d.getSeverity() == Diagnostic.Severity.WARNING).count();
    }

    public String formatReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================================================================\n");
        sb.append("                   SPRING BOOT & THYMELEAF RELATIONSHIP ANALYSIS REPORT\n");
        sb.append("====================================================================================================\n\n");

        sb.append("--- CONTROLLER TO TEMPLATE RELATIONSHIPS ---\n");
        for (RelationshipEntry rel : relationships) {
            sb.append(String.format("* Endpoint:           [%s] %s\n", rel.getHttpMethod(), rel.getEndpointPath()));
            sb.append(String.format("  Controller Method:  %s.%s()\n", rel.getControllerName(), rel.getMethodName()));
            sb.append(String.format("  Returned Template:  %s\n", rel.getReturnedTemplate()));
            sb.append(String.format("  Model Attributes:   %s\n", rel.getModelAttributes().isEmpty() ? "(none)" : rel.getModelAttributes()));
            sb.append(String.format("  Resolved Vars:      %s\n", rel.getResolvedVariables().isEmpty() ? "(none)" : rel.getResolvedVariables()));
            if (!rel.getUnresolvedVariables().isEmpty()) {
                sb.append(String.format("  Unresolved Vars:    %s\n", rel.getUnresolvedVariables()));
            }
            sb.append(String.format("  Relationship Status: %s (%s)\n", rel.isValid() ? "VALID [OK]" : "INVALID [FAIL]", rel.getStatusMessage()));
            sb.append("----------------------------------------------------------------------------------------------------\n");
        }

        sb.append("\n--- DIAGNOSTICS & SEMANTIC VALIDATION ---\n");
        if (diagnostics.isEmpty()) {
            sb.append("[OK] No errors or warnings found! All controller templates and model attributes are valid.\n");
        } else {
            for (Diagnostic d : diagnostics) {
                sb.append(d.toString()).append("\n");
            }
        }
        sb.append("====================================================================================================\n");
        sb.append(String.format("Summary: %d Relationships analyzed | %d Errors | %d Warnings\n",
                relationships.size(), getErrorCount(), getWarningCount()));
        sb.append("====================================================================================================\n");
        return sb.toString();
    }
}
