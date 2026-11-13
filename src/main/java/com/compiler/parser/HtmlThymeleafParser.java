package com.compiler.parser;

import com.compiler.ast.html.*;
import com.compiler.lexer.HtmlLexer;
import com.compiler.lexer.Token;
import com.compiler.lexer.TokenType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class HtmlThymeleafParser {
    private final List<Token> tokens;
    private final String templatePath;
    private final String templateName;
    private final String sourceFile;
    private int cursor = 0;

    public HtmlThymeleafParser(List<Token> tokens, String templatePath, String templateName, String sourceFile) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.templatePath = templatePath != null ? templatePath : "";
        this.templateName = templateName != null ? templateName : "";
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public HtmlThymeleafParser(String htmlSource, String templatePath, String templateName) {
        this(new HtmlLexer(htmlSource, templatePath).tokenize(), templatePath, templateName, templatePath);
    }

    public TemplateNode parse() throws ParserException {
        TemplateNode templateNode = new TemplateNode(templatePath, templateName, 1, sourceFile);
        Deque<HtmlElementNode> elementStack = new ArrayDeque<>();

        while (!isAtEnd()) {
            Token t = peek();

            // Skip DOCTYPE
            if (t.getType() == TokenType.HTML_DOCTYPE) {
                advance();
                continue;
            }

            // Text node
            if (t.getType() == TokenType.HTML_TEXT) {
                Token textToken = advance();
                TextNode textNode = new TextNode(textToken.getValue(), textToken.getLine(), sourceFile);
                if (!elementStack.isEmpty()) {
                    elementStack.peek().addChild(textNode);
                } else {
                    templateNode.addChild(textNode);
                }
                continue;
            }

            // Closing tag: </tag>
            if (t.getType() == TokenType.HTML_TAG_CLOSE_START) {
                advance(); // </
                String closeTagName = "";
                if (check(TokenType.HTML_TAG_NAME)) {
                    closeTagName = advance().getValue();
                }
                if (check(TokenType.HTML_TAG_END)) {
                    advance(); // >
                }

                // Pop matching element or parent
                if (!elementStack.isEmpty()) {
                    if (closeTagName.isEmpty() || elementStack.peek().getTagName().equalsIgnoreCase(closeTagName)) {
                        elementStack.pop();
                    } else {
                        // Look for tag in stack
                        boolean found = false;
                        for (HtmlElementNode node : elementStack) {
                            if (node.getTagName().equalsIgnoreCase(closeTagName)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            while (!elementStack.isEmpty() && !elementStack.peek().getTagName().equalsIgnoreCase(closeTagName)) {
                                elementStack.pop();
                            }
                            if (!elementStack.isEmpty()) {
                                elementStack.pop();
                            }
                        }
                    }
                }
                continue;
            }

            // Opening tag: <tag ... > or <tag ... />
            if (t.getType() == TokenType.HTML_TAG_OPEN_START) {
                HtmlElementNode element = parseElementHeader();
                if (element == null) continue;

                if (!elementStack.isEmpty()) {
                    elementStack.peek().addChild(element);
                } else {
                    templateNode.addRootElement(element);
                }

                // If not self-closing and not a void element, push to stack
                if (!element.isSelfClosing() && !isVoidElement(element.getTagName())) {
                    elementStack.push(element);
                }
                continue;
            }

            // Any other token
            advance();
        }

        return templateNode;
    }

    private HtmlElementNode parseElementHeader() {
        Token openStart = advance(); // '<'
        if (!check(TokenType.HTML_TAG_NAME)) {
            return null;
        }

        Token tagToken = advance();
        String tagName = tagToken.getValue();
        HtmlElementNode element = new HtmlElementNode(tagName, false, tagToken.getLine(), sourceFile);

        // Attributes
        while (!isAtEnd() && !check(TokenType.HTML_TAG_END) && !check(TokenType.HTML_TAG_SELF_CLOSE)) {
            Token attrToken = advance();
            String attrName = attrToken.getValue();
            String attrValue = "";

            if (check(TokenType.ASSIGN)) {
                advance(); // '='
                if (check(TokenType.HTML_ATTR_VALUE)) {
                    attrValue = advance().getValue();
                }
            }

            if (attrName.toLowerCase().startsWith("th:")) {
                ThymeleafAttributeNode thAttr = new ThymeleafAttributeNode(attrName, attrValue, attrToken.getLine(), sourceFile);
                ThymeleafExpressionNode exprNode = parseThymeleafExpression(attrName, attrValue, attrToken.getLine());
                thAttr.setExpressionNode(exprNode);
                element.addThymeleafAttribute(thAttr);
            } else {
                element.addStandardAttribute(new HtmlAttributeNode(attrName, attrValue, attrToken.getLine(), sourceFile));
            }
        }

        boolean selfClose = false;
        if (check(TokenType.HTML_TAG_SELF_CLOSE)) {
            advance(); // '/>'
            selfClose = true;
        } else if (check(TokenType.HTML_TAG_END)) {
            advance(); // '>'
        }

        if (selfClose || isVoidElement(tagName)) {
            return new HtmlElementNode(tagName, true, tagToken.getLine(), sourceFile) {{
                for (HtmlAttributeNode a : element.getStandardAttributes()) addStandardAttribute(a);
                for (ThymeleafAttributeNode a : element.getThymeleafAttributes()) addThymeleafAttribute(a);
            }};
        }

        return element;
    }

    private ThymeleafExpressionNode parseThymeleafExpression(String directive, String rawExpr, int line) {
        String trimmed = rawExpr != null ? rawExpr.trim() : "";

        // Check for th:each: e.g. "product : ${products}" or "item, iter : ${items}"
        if ("th:each".equalsIgnoreCase(directive)) {
            int colonIndex = trimmed.indexOf(':');
            if (colonIndex != -1) {
                String iterVar = trimmed.substring(0, colonIndex).trim();
                if (iterVar.contains(",")) {
                    iterVar = iterVar.split(",")[0].trim();
                }
                String collectionExpr = trimmed.substring(colonIndex + 1).trim();
                String collectionVar = extractBaseVariable(collectionExpr);
                return new ThymeleafExpressionNode(
                        ThymeleafExpressionNode.ExpressionKind.ITERATION,
                        trimmed,
                        collectionVar,
                        "",
                        iterVar,
                        line,
                        sourceFile
                );
            }
        }

        // Variable expression ${...}
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            String inner = trimmed.substring(2, trimmed.length() - 1).trim();
            String baseVar = extractBaseVariable(inner);
            String propPath = extractPropertyPath(inner);
            return new ThymeleafExpressionNode(
                    ThymeleafExpressionNode.ExpressionKind.VARIABLE,
                    trimmed,
                    baseVar,
                    propPath,
                    "",
                    line,
                    sourceFile
            );
        }

        // Selection expression *{...}
        if (trimmed.startsWith("*{") && trimmed.endsWith("}")) {
            String inner = trimmed.substring(2, trimmed.length() - 1).trim();
            String baseVar = extractBaseVariable(inner);
            String propPath = extractPropertyPath(inner);
            return new ThymeleafExpressionNode(
                    ThymeleafExpressionNode.ExpressionKind.SELECTION,
                    trimmed,
                    baseVar,
                    propPath,
                    "",
                    line,
                    sourceFile
            );
        }

        // Link expression @{...}
        if (trimmed.startsWith("@{") && trimmed.endsWith("}")) {
            return new ThymeleafExpressionNode(
                    ThymeleafExpressionNode.ExpressionKind.LINK,
                    trimmed,
                    "",
                    "",
                    "",
                    line,
                    sourceFile
            );
        }

        // Literal or un-bracketed expression
        return new ThymeleafExpressionNode(
                ThymeleafExpressionNode.ExpressionKind.LITERAL,
                trimmed,
                extractBaseVariable(trimmed),
                "",
                "",
                line,
                sourceFile
        );
    }

    private String extractBaseVariable(String expr) {
        if (expr == null || expr.isEmpty()) return "";
        String clean = expr;
        if (clean.startsWith("${") && clean.endsWith("}")) {
            clean = clean.substring(2, clean.length() - 1).trim();
        } else if (clean.startsWith("*{") && clean.endsWith("}")) {
            clean = clean.substring(2, clean.length() - 1).trim();
        }

        // Handle method call or property navigation
        int dotIndex = clean.indexOf('.');
        int parenIndex = clean.indexOf('(');
        int cut = clean.length();
        if (dotIndex != -1) cut = Math.min(cut, dotIndex);
        if (parenIndex != -1) cut = Math.min(cut, parenIndex);

        return clean.substring(0, cut).trim();
    }

    private String extractPropertyPath(String expr) {
        if (expr == null || expr.isEmpty()) return "";
        String clean = expr;
        if (clean.startsWith("${") && clean.endsWith("}")) {
            clean = clean.substring(2, clean.length() - 1).trim();
        } else if (clean.startsWith("*{") && clean.endsWith("}")) {
            clean = clean.substring(2, clean.length() - 1).trim();
        }

        int dotIndex = clean.indexOf('.');
        if (dotIndex != -1 && dotIndex + 1 < clean.length()) {
            return clean.substring(dotIndex + 1).trim();
        }
        return "";
    }

    private boolean isVoidElement(String tag) {
        String lower = tag.toLowerCase();
        return lower.equals("meta") || lower.equals("link") || lower.equals("img") ||
                lower.equals("input") || lower.equals("br") || lower.equals("hr");
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) cursor++;
        return previous();
    }

    private boolean isAtEnd() {
        return cursor >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        if (cursor >= tokens.size()) {
            return new Token(TokenType.EOF, "", 0, 0, sourceFile);
        }
        return tokens.get(cursor);
    }

    private Token previous() {
        return tokens.get(cursor - 1);
    }
}
