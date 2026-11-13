package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;

public class HtmlLexer implements Lexer {
    private final String source;
    private final String sourceFile;
    private int cursor = 0;
    private int line = 1;
    private int column = 1;

    public HtmlLexer(String source, String sourceFile) {
        this.source = source != null ? source : "";
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public HtmlLexer(String source) {
        this(source, "Template.html");
    }

    @Override
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            // Check for tags
            if (peek() == '<') {
                int startLine = line;
                int startCol = column;

                // Check for comment <!-- ... -->
                if (match("<!--")) {
                    skipComment();
                    continue;
                }

                // Check for DOCTYPE <!DOCTYPE ... >
                if (match("<!DOCTYPE") || match("<!doctype")) {
                    StringBuilder dt = new StringBuilder("<!DOCTYPE");
                    while (!isAtEnd() && peek() != '>') {
                        dt.append(advance());
                    }
                    if (!isAtEnd() && peek() == '>') {
                        dt.append(advance());
                    }
                    tokens.add(new Token(TokenType.HTML_DOCTYPE, dt.toString(), startLine, startCol, sourceFile));
                    continue;
                }

                // Check for closing tag </tag>
                if (peekNext() == '/') {
                    advance(); // '<'
                    advance(); // '/'
                    tokens.add(new Token(TokenType.HTML_TAG_CLOSE_START, "</", startLine, startCol, sourceFile));

                    skipWhitespace();
                    String tagName = scanTagName();
                    if (!tagName.isEmpty()) {
                        tokens.add(new Token(TokenType.HTML_TAG_NAME, tagName, line, column, sourceFile));
                    }
                    skipWhitespace();
                    if (peek() == '>') {
                        advance();
                        tokens.add(new Token(TokenType.HTML_TAG_END, ">", line, column, sourceFile));
                    }
                    continue;
                }

                // Opening tag <tag ... >
                advance(); // consume '<'
                tokens.add(new Token(TokenType.HTML_TAG_OPEN_START, "<", startLine, startCol, sourceFile));

                skipWhitespace();
                String tagName = scanTagName();
                if (!tagName.isEmpty()) {
                    tokens.add(new Token(TokenType.HTML_TAG_NAME, tagName, line, column, sourceFile));
                }

                // Scan attributes until '>' or '/>'
                while (!isAtEnd() && peek() != '>' && !(peek() == '/' && peekNext() == '>')) {
                    skipWhitespace();
                    if (isAtEnd() || peek() == '>' || (peek() == '/' && peekNext() == '>')) break;

                    int attrLine = line;
                    int attrCol = column;
                    String attrName = scanAttrName();
                    if (attrName.isEmpty()) {
                        advance();
                        continue;
                    }

                    TokenType attrTokenType = resolveAttrTokenType(attrName);
                    tokens.add(new Token(attrTokenType, attrName, attrLine, attrCol, sourceFile));

                    skipWhitespace();
                    if (peek() == '=') {
                        advance();
                        tokens.add(new Token(TokenType.ASSIGN, "=", line, column, sourceFile));
                        skipWhitespace();

                        if (peek() == '"' || peek() == '\'') {
                            tokens.add(scanAttrValue());
                        } else {
                            // Unquoted attribute value
                            tokens.add(scanUnquotedAttrValue());
                        }
                    }
                }

                skipWhitespace();
                if (peek() == '/' && peekNext() == '>') {
                    int endLine = line;
                    int endCol = column;
                    advance(); // '/'
                    advance(); // '>'
                    tokens.add(new Token(TokenType.HTML_TAG_SELF_CLOSE, "/>", endLine, endCol, sourceFile));
                } else if (peek() == '>') {
                    advance(); // '>'
                    tokens.add(new Token(TokenType.HTML_TAG_END, ">", line, column, sourceFile));
                }
            } else {
                // Text content between tags
                int textLine = line;
                int textCol = column;
                StringBuilder text = new StringBuilder();

                while (!isAtEnd() && peek() != '<') {
                    if (peek() == '\n') {
                        line++;
                        column = 0;
                    }
                    text.append(advance());
                }

                String trimmed = text.toString().trim();
                if (!trimmed.isEmpty()) {
                    tokens.add(new Token(TokenType.HTML_TEXT, trimmed, textLine, textCol, sourceFile));
                }
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column, sourceFile));
        return tokens;
    }

    private TokenType resolveAttrTokenType(String attrName) {
        String lower = attrName.toLowerCase();
        switch (lower) {
            case "th:text": return TokenType.TH_ATTR_TEXT;
            case "th:each": return TokenType.TH_ATTR_EACH;
            case "th:if": return TokenType.TH_ATTR_IF;
            case "th:unless": return TokenType.TH_ATTR_UNLESS;
            case "th:href": return TokenType.TH_ATTR_HREF;
            case "th:src": return TokenType.TH_ATTR_SRC;
            case "th:value": return TokenType.TH_ATTR_VALUE;
            case "th:object": return TokenType.TH_ATTR_OBJECT;
            case "th:action": return TokenType.TH_ATTR_ACTION;
            default:
                if (lower.startsWith("th:")) {
                    return TokenType.TH_ATTR_OTHER;
                }
                return TokenType.HTML_ATTR_NAME;
        }
    }

    private String scanTagName() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && (isAlphaNumeric(peek()) || peek() == '-' || peek() == ':')) {
            sb.append(advance());
        }
        return sb.toString();
    }

    private String scanAttrName() {
        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && (isAlphaNumeric(peek()) || peek() == '-' || peek() == ':' || peek() == '@')) {
            sb.append(advance());
        }
        return sb.toString();
    }

    private Token scanAttrValue() {
        int startLine = line;
        int startCol = column;
        char quote = advance(); // '"' or '\''
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && peek() != quote) {
            if (peek() == '\n') {
                line++;
                column = 0;
            }
            sb.append(advance());
        }

        if (!isAtEnd() && peek() == quote) {
            advance(); // consume quote
        }

        return new Token(TokenType.HTML_ATTR_VALUE, sb.toString(), startLine, startCol, sourceFile);
    }

    private Token scanUnquotedAttrValue() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && !Character.isWhitespace(peek()) && peek() != '>' && peek() != '/') {
            sb.append(advance());
        }

        return new Token(TokenType.HTML_ATTR_VALUE, sb.toString(), startLine, startCol, sourceFile);
    }

    private void skipComment() {
        while (!isAtEnd()) {
            if (match("-->")) {
                break;
            }
            if (peek() == '\n') {
                line++;
                column = 1;
                cursor++;
            } else {
                advance();
            }
        }
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
                line++;
                column = 1;
                cursor++;
            } else {
                break;
            }
        }
    }

    private boolean match(String prefix) {
        if (source.startsWith(prefix, cursor)) {
            for (int i = 0; i < prefix.length(); i++) {
                advance();
            }
            return true;
        }
        return false;
    }

    private boolean isAtEnd() {
        return cursor >= source.length();
    }

    private char advance() {
        if (isAtEnd()) return '\0';
        char c = source.charAt(cursor++);
        column++;
        return c;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(cursor);
    }

    private char peekNext() {
        if (cursor + 1 >= source.length()) return '\0';
        return source.charAt(cursor + 1);
    }

    private boolean isAlphaNumeric(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_';
    }
}
