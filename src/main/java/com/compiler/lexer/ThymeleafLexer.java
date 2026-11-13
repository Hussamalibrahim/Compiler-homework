package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer for Thymeleaf expression strings such as:
 *   ${products}
 *   ${product.name}
 *   product : ${products}
 *   @{/products/details/{id}(id=${product.id})}
 *   *{name}
 */
public class ThymeleafLexer implements Lexer {
    private final String source;
    private final String sourceFile;
    private final int baseLine;
    private int cursor = 0;
    private int line;
    private int column = 1;

    public ThymeleafLexer(String source, String sourceFile, int baseLine) {
        this.source = source != null ? source : "";
        this.sourceFile = sourceFile != null ? sourceFile : "";
        this.baseLine = Math.max(1, baseLine);
        this.line = this.baseLine;
    }

    public ThymeleafLexer(String source) {
        this(source, "ThymeleafExpr", 1);
    }

    @Override
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            char c = peek();
            int startLine = line;
            int startCol = column;

            // Variable expression ${...}
            if (c == '$' && peekNext() == '{') {
                tokens.add(scanEnclosedExpr('$', TokenType.TH_EXPR_VARIABLE));
                continue;
            }

            // Selection expression *{...}
            if (c == '*' && peekNext() == '{') {
                tokens.add(scanEnclosedExpr('*', TokenType.TH_EXPR_SELECTION));
                continue;
            }

            // Link expression @{...}
            if (c == '@' && peekNext() == '{') {
                tokens.add(scanEnclosedExpr('@', TokenType.TH_EXPR_LINK));
                continue;
            }

            // Separators & operators
            if (c == ':') {
                advance();
                tokens.add(new Token(TokenType.COLON, ":", startLine, startCol, sourceFile));
                continue;
            }
            if (c == '.') {
                advance();
                tokens.add(new Token(TokenType.DOT, ".", startLine, startCol, sourceFile));
                continue;
            }
            if (c == ',') {
                advance();
                tokens.add(new Token(TokenType.COMMA, ",", startLine, startCol, sourceFile));
                continue;
            }
            if (c == '(') {
                advance();
                tokens.add(new Token(TokenType.LPAREN, "(", startLine, startCol, sourceFile));
                continue;
            }
            if (c == ')') {
                advance();
                tokens.add(new Token(TokenType.RPAREN, ")", startLine, startCol, sourceFile));
                continue;
            }
            if (c == '=') {
                advance();
                tokens.add(new Token(TokenType.ASSIGN, "=", startLine, startCol, sourceFile));
                continue;
            }

            // Identifiers / words
            if (isAlpha(c)) {
                StringBuilder sb = new StringBuilder();
                while (!isAtEnd() && (isAlphaNumeric(peek()) || peek() == '_')) {
                    sb.append(advance());
                }
                tokens.add(new Token(TokenType.IDENTIFIER, sb.toString(), startLine, startCol, sourceFile));
                continue;
            }

            // Literals or other characters
            if (c == '\'' || c == '"') {
                tokens.add(scanString(c));
                continue;
            }

            // Skip or collect unknown
            tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(advance()), startLine, startCol, sourceFile));
        }

        tokens.add(new Token(TokenType.EOF, "", line, column, sourceFile));
        return tokens;
    }

    private Token scanEnclosedExpr(char prefix, TokenType tokenType) {
        int startLine = line;
        int startCol = column;
        advance(); // consume prefix ($ or * or @)
        advance(); // consume '{'

        StringBuilder inner = new StringBuilder();
        int braceDepth = 1;

        while (!isAtEnd() && braceDepth > 0) {
            char c = peek();
            if (c == '{') {
                braceDepth++;
                inner.append(advance());
            } else if (c == '}') {
                braceDepth--;
                if (braceDepth > 0) {
                    inner.append(advance());
                } else {
                    advance(); // consume final '}'
                }
            } else {
                if (c == '\n') {
                    line++;
                    column = 0;
                }
                inner.append(advance());
            }
        }

        return new Token(tokenType, inner.toString().trim(), startLine, startCol, sourceFile);
    }

    private Token scanString(char quote) {
        int startLine = line;
        int startCol = column;
        advance(); // consume opening quote
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && peek() != quote) {
            sb.append(advance());
        }
        if (!isAtEnd() && peek() == quote) {
            advance(); // consume closing quote
        }

        return new Token(TokenType.STRING_LITERAL, sb.toString(), startLine, startCol, sourceFile);
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

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || (c >= '0' && c <= '9');
    }
}
