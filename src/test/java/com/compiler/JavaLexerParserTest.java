package com.compiler;

import com.compiler.ast.java.ClassNode;
import com.compiler.ast.java.ControllerNode;
import com.compiler.ast.java.MethodNode;
import com.compiler.lexer.JavaLexer;
import com.compiler.lexer.Token;
import com.compiler.parser.JavaParser;
import com.compiler.parser.ParserException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JavaLexerParserTest {

    @Test
    public void testJavaLexer() {
        String code = "@Controller\npublic class ProductController {\n" +
                "    @GetMapping(\"/products\")\n" +
                "    public String list(Model model) {\n" +
                "        model.addAttribute(\"products\", items);\n" +
                "        return \"products/list\";\n" +
                "    }\n" +
                "}";

        JavaLexer lexer = new JavaLexer(code, "ProductController.java");
        List<Token> tokens = lexer.tokenize();

        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
        assertTrue(tokens.stream().anyMatch(t -> t.getValue().equals("@Controller")));
        assertTrue(tokens.stream().anyMatch(t -> t.getValue().equals("@GetMapping")));
        assertTrue(tokens.stream().anyMatch(t -> t.getValue().equals("addAttribute")));
    }

    @Test
    public void testJavaParserController() throws ParserException {
        String code = "package com.store;\n\n" +
                "@Controller\n" +
                "public class ProductController {\n" +
                "    @GetMapping(\"/products\")\n" +
                "    public String list(Model model) {\n" +
                "        model.addAttribute(\"products\", service.getAll());\n" +
                "        return \"products/list\";\n" +
                "    }\n" +
                "}";

        JavaLexer lexer = new JavaLexer(code, "ProductController.java");
        JavaParser parser = new JavaParser(lexer.tokenize(), "ProductController.java");
        ClassNode node = parser.parse();

        assertNotNull(node);
        assertTrue(node instanceof ControllerNode);
        ControllerNode ctrl = (ControllerNode) node;
        assertEquals("ProductController", ctrl.getClassName());
        assertEquals("com.store", ctrl.getPackageName());
        assertEquals(1, ctrl.getMethods().size());

        MethodNode method = ctrl.getMethods().get(0);
        assertEquals("list", method.getMethodName());
        assertEquals("String", method.getReturnType());
        assertNotNull(method.getEndpoint());
        assertEquals("GET", method.getEndpoint().getHttpMethod());
        assertEquals("/products", method.getEndpoint().getPath());
        assertEquals(1, method.getModelAttributes().size());
        assertEquals("products", method.getModelAttributes().get(0).getAttributeName());
        assertNotNull(method.getReturnStatement());
        assertEquals("products/list", method.getReturnStatement().getCleanTemplateName());
    }
}
