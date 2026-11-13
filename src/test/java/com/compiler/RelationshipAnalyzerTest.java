package com.compiler;

import com.compiler.analyzer.AnalysisReport;
import com.compiler.analyzer.Diagnostic;
import com.compiler.analyzer.RelationshipAnalyzer;
import com.compiler.ast.ProjectNode;
import com.compiler.ast.html.TemplateNode;
import com.compiler.ast.java.ClassNode;
import com.compiler.lexer.HtmlLexer;
import com.compiler.lexer.JavaLexer;
import com.compiler.parser.HtmlThymeleafParser;
import com.compiler.parser.JavaParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RelationshipAnalyzerTest {

    @Test
    public void testValidRelationship() throws Exception {
        String javaCode = "@Controller\n" +
                "public class ProductController {\n" +
                "    @GetMapping(\"/products\")\n" +
                "    public String list(Model model) {\n" +
                "        model.addAttribute(\"products\", list);\n" +
                "        return \"products/list\";\n" +
                "    }\n" +
                "}";

        String htmlCode = "<div th:each=\"product : ${products}\">\n" +
                "    <span th:text=\"${product.name}\"></span>\n" +
                "</div>";

        ProjectNode project = new ProjectNode("TestApp");

        JavaParser javaParser = new JavaParser(new JavaLexer(javaCode, "ProductController.java").tokenize(), "ProductController.java");
        ClassNode controller = javaParser.parse();
        project.addChild(controller);

        HtmlThymeleafParser htmlParser = new HtmlThymeleafParser(
                new HtmlLexer(htmlCode, "products/list.html").tokenize(),
                "products/list.html",
                "list.html",
                "products/list.html");
        TemplateNode template = htmlParser.parse();
        project.addChild(template);

        RelationshipAnalyzer analyzer = new RelationshipAnalyzer(project);
        AnalysisReport report = analyzer.analyze();

        assertEquals(1, report.getRelationships().size());
        AnalysisReport.RelationshipEntry rel = report.getRelationships().get(0);
        assertTrue(rel.isValid());
        assertEquals("products/list", rel.getReturnedTemplate());
        assertEquals(0, report.getErrorCount());
    }

    @Test
    public void testMissingTemplateDetection() throws Exception {
        String javaCode = "@Controller\n" +
                "public class ProductController {\n" +
                "    @GetMapping(\"/products\")\n" +
                "    public String list() {\n" +
                "        return \"products/non_existent\";\n" +
                "    }\n" +
                "}";

        ProjectNode project = new ProjectNode("TestApp");
        JavaParser javaParser = new JavaParser(new JavaLexer(javaCode, "ProductController.java").tokenize(), "ProductController.java");
        project.addChild(javaParser.parse());

        RelationshipAnalyzer analyzer = new RelationshipAnalyzer(project);
        AnalysisReport report = analyzer.analyze();

        assertEquals(1, report.getErrorCount());
        assertTrue(report.getDiagnostics().stream().anyMatch(d -> d.getCategory().equals("MISSING_TEMPLATE")));
    }

    @Test
    public void testUnresolvedVariableDetection() throws Exception {
        String javaCode = "@Controller\n" +
                "public class ProductController {\n" +
                "    @GetMapping(\"/products\")\n" +
                "    public String list(Model model) {\n" +
                "        return \"products/list\";\n" +
                "    }\n" +
                "}";

        String htmlCode = "<div>\n" +
                "    <span th:text=\"${unknownVariable}\"></span>\n" +
                "</div>";

        ProjectNode project = new ProjectNode("TestApp");
        JavaParser javaParser = new JavaParser(new JavaLexer(javaCode, "ProductController.java").tokenize(), "ProductController.java");
        project.addChild(javaParser.parse());

        HtmlThymeleafParser htmlParser = new HtmlThymeleafParser(
                new HtmlLexer(htmlCode, "products/list.html").tokenize(),
                "products/list.html",
                "list.html",
                "products/list.html");
        project.addChild(htmlParser.parse());

        RelationshipAnalyzer analyzer = new RelationshipAnalyzer(project);
        AnalysisReport report = analyzer.analyze();

        assertEquals(1, report.getErrorCount());
        assertTrue(report.getDiagnostics().stream().anyMatch(d -> d.getCategory().equals("UNRESOLVED_VARIABLE")));
    }
}
