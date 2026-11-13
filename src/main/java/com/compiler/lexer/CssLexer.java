package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;

public class CssLexer implements Lexer {
    private final String source;
    private final String sourceFile;
    private int cursor = 0;
    private int line = 1;
    private int column = 1;

    public CssLexer(String source, String sourceFile) {
        this.source = source != null ? source : "";
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public CssLexer(String source) {
        this(source, "styles.css");
    }

    @Override
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        boolean insideBlock = false;

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            // Comments /* ... */
            if (peek() == '/' && peekNext() == '*') {
                skipComment();
                continue;
            }

            int startLine = line;
            int startCol = column;
            char c = peek();

            if (c == '{') {
                advance();
                insideBlock = true;
                tokens.add(new Token(TokenType.LBRACE, "{", startLine, startCol, sourceFile));
                continue;
            }

            if (c == '}') {
                advance();
                insideBlock = false;
                tokens.add(new Token(TokenType.RBRACE, "}", startLine, startCol, sourceFile));
                continue;
            }

            if (!insideBlock) {
                // We are scanning selector(s) up to '{'
                StringBuilder selector = new StringBuilder();
                while (!isAtEnd() && peek() != '{' && peek() != '/') {
                    if (peek() == '\n') {
                        line++;
                        column = 0;
                    }
                    selector.append(advance());
                }

                String sel = selector.toString().trim();
                if (!sel.isEmpty()) {
                    tokens.add(new Token(TokenType.CSS_SELECTOR, sel, startLine, startCol, sourceFile));
                }
            } else {
                // Inside declaration block: property : value ;
                // 1. Property
                StringBuilder prop = new StringBuilder();
                while (!isAtEnd() && peek() != ':' && peek() != '}') {
                    if (peek() == '\n') {
                        line++;
                        column = 0;
                    }
                    prop.append(advance());
                }

                String propertyName = prop.toString().trim();
                if (!propertyName.isEmpty()) {
                    tokens.add(new Token(TokenType.CSS_PROPERTY, propertyName, startLine, startCol, sourceFile));
                }

                skipWhitespace();
                if (peek() == ':') {
                    int colonLine = line;
                    int colonCol = column;
                    advance();
                    tokens.add(new Token(TokenType.COLON, ":", colonLine, colonCol, sourceFile));
                    skipWhitespace();

                    // 2. Value up to ';' or '}'
                    int valLine = line;
                    int valCol = column;
                    StringBuilder val = new StringBuilder();
                    while (!isAtEnd() && peek() != ';' && peek() != '}') {
                        if (peek() == '\n') {
                            line++;
                            column = 0;
                        }
                        val.append(advance());
                    }

                    String valStr = val.toString().trim();
                    if (!valStr.isEmpty()) {
                        tokens.add(new Token(TokenType.CSS_VALUE, valStr, valLine, valCol, sourceFile));
                    }

                    skipWhitespace();
                    if (peek() == ';') {
                        tokens.add(new Token(TokenType.SEMICOLON, ";", line, column, sourceFile));
                        advance();
                    }
                }
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column, sourceFile));
        return tokens;
    }

    private void skipComment() {
        advance(); // '/'
        advance(); // '*'
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // '*'
                advance(); // '/'
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
}
