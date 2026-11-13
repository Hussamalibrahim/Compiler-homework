package com.compiler;

import com.compiler.ast.html.HtmlElementNode;
import com.compiler.ast.html.TemplateNode;
import com.compiler.ast.html.ThymeleafAttributeNode;
import com.compiler.ast.html.ThymeleafExpressionNode;
import com.compiler.parser.HtmlThymeleafParser;
import com.compiler.parser.ParserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlThymeleafParserTest {

    @Test
    public void testThymeleafTemplateParsing() throws ParserException {
        String html = "<div class=\"container\">\n" +
                "    <div th:each=\"product : ${products}\">\n" +
                "        <h2 th:text=\"${product.name}\">Name</h2>\n" +
                "        <span th:text=\"${product.price}\">0.0</span>\n" +
                "    </div>\n" +
                "</div>";

        HtmlThymeleafParser parser = new HtmlThymeleafParser(html, "products/list.html", "list.html");
        TemplateNode template = parser.parse();

        assertNotNull(template);
        assertEquals("list.html", template.getTemplateName());
        assertEquals(1, template.getRootElements().size());

        HtmlElementNode root = template.getRootElements().get(0);
        assertEquals("div", root.getTagName());

        HtmlElementNode innerDiv = (HtmlElementNode) root.getChildren().stream()
                .filter(c -> c instanceof HtmlElementNode)
                .findFirst().orElse(null);

        assertNotNull(innerDiv);
        ThymeleafAttributeNode eachAttr = innerDiv.getThymeleafAttribute("th:each");
        assertNotNull(eachAttr);
        assertNotNull(eachAttr.getExpressionNode());
        assertEquals("product", eachAttr.getExpressionNode().getIterationVariable());
        assertEquals("products", eachAttr.getExpressionNode().getBaseVariable());
    }
}
