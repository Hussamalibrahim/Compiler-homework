package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaLexer implements Lexer {
    private final String source;
    private final String sourceFile;
    private int cursor = 0;
    private int line = 1;
    private int column = 1;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    private static final Map<String, TokenType> TYPES = new HashMap<>();
    private static final Map<String, TokenType> ANNOTATIONS = new HashMap<>();

    static {
        KEYWORDS.put("package", TokenType.KEYWORD_PACKAGE);
        KEYWORDS.put("import", TokenType.KEYWORD_IMPORT);
        KEYWORDS.put("public", TokenType.KEYWORD_PUBLIC);
        KEYWORDS.put("private", TokenType.KEYWORD_PRIVATE);
        KEYWORDS.put("protected", TokenType.KEYWORD_PROTECTED);
        KEYWORDS.put("class", TokenType.KEYWORD_CLASS);
        KEYWORDS.put("return", TokenType.KEYWORD_RETURN);
        KEYWORDS.put("void", TokenType.KEYWORD_VOID);
        KEYWORDS.put("new", TokenType.KEYWORD_NEW);
        KEYWORDS.put("if", TokenType.KEYWORD_IF);
        KEYWORDS.put("else", TokenType.KEYWORD_ELSE);
        KEYWORDS.put("for", TokenType.KEYWORD_FOR);
        KEYWORDS.put("while", TokenType.KEYWORD_WHILE);

        TYPES.put("String", TokenType.TYPE_STRING);
        TYPES.put("int", TokenType.TYPE_INT);
        TYPES.put("double", TokenType.TYPE_DOUBLE);
        TYPES.put("boolean", TokenType.TYPE_BOOLEAN);
        TYPES.put("long", TokenType.TYPE_LONG);
        TYPES.put("Model", TokenType.TYPE_MODEL);
        TYPES.put("List", TokenType.TYPE_LIST);

        ANNOTATIONS.put("@Controller", TokenType.ANNOTATION_CONTROLLER);
        ANNOTATIONS.put("@RestController", TokenType.ANNOTATION_REST_CONTROLLER);
        ANNOTATIONS.put("@GetMapping", TokenType.ANNOTATION_GET_MAPPING);
        ANNOTATIONS.put("@PostMapping", TokenType.ANNOTATION_POST_MAPPING);
        ANNOTATIONS.put("@PathVariable", TokenType.ANNOTATION_PATH_VARIABLE);
        ANNOTATIONS.put("@RequestParam", TokenType.ANNOTATION_REQUEST_PARAM);
        ANNOTATIONS.put("@ModelAttribute", TokenType.ANNOTATION_MODEL_ATTRIBUTE);
    }

    public JavaLexer(String source, String sourceFile) {
        this.source = source != null ? source : "";
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public JavaLexer(String source) {
        this(source, "JavaSource.java");
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

            // Single-line or multi-line comments
            if (c == '/' && peekNext() == '/') {
                skipSingleLineComment();
                continue;
            }
            if (c == '/' && peekNext() == '*') {
                skipMultiLineComment();
                continue;
            }

            // Annotations (@Controller, @GetMapping, etc.)
            if (c == '@') {
                tokens.add(scanAnnotation());
                continue;
            }

            // String literals
            if (c == '"') {
                tokens.add(scanStringLiteral());
                continue;
            }

            // Numbers
            if (isDigit(c)) {
                tokens.add(scanNumberLiteral());
                continue;
            }

            // Identifiers / Keywords / Types
            if (isAlpha(c)) {
                tokens.add(scanIdentifierOrKeyword());
                continue;
            }

            // Operators & Punctuation
            advance();
            switch (c) {
                case '(': tokens.add(new Token(TokenType.LPAREN, "(", startLine, startCol, sourceFile)); break;
                case ')': tokens.add(new Token(TokenType.RPAREN, ")", startLine, startCol, sourceFile)); break;
                case '{': tokens.add(new Token(TokenType.LBRACE, "{", startLine, startCol, sourceFile)); break;
                case '}': tokens.add(new Token(TokenType.RBRACE, "}", startLine, startCol, sourceFile)); break;
                case '[': tokens.add(new Token(TokenType.LBRACKET, "[", startLine, startCol, sourceFile)); break;
                case ']': tokens.add(new Token(TokenType.RBRACKET, "]", startLine, startCol, sourceFile)); break;
                case ';': tokens.add(new Token(TokenType.SEMICOLON, ";", startLine, startCol, sourceFile)); break;
                case ',': tokens.add(new Token(TokenType.COMMA, ",", startLine, startCol, sourceFile)); break;
                case '.': tokens.add(new Token(TokenType.DOT, ".", startLine, startCol, sourceFile)); break;
                case ':': tokens.add(new Token(TokenType.COLON, ":", startLine, startCol, sourceFile)); break;
                case '=': tokens.add(new Token(TokenType.ASSIGN, "=", startLine, startCol, sourceFile)); break;
                case '-':
                    if (peek() == '>') {
                        advance();
                        tokens.add(new Token(TokenType.ARROW, "->", startLine, startCol, sourceFile));
                    } else {
                        tokens.add(new Token(TokenType.UNKNOWN, "-", startLine, startCol, sourceFile));
                    }
                    break;
                default:
                    tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(c), startLine, startCol, sourceFile));
                    break;
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column, sourceFile));
        return tokens;
    }

    private Token scanAnnotation() {
        int startLine = line;
        int startCol = column;
        advance(); // consume '@'

        StringBuilder sb = new StringBuilder("@");
        while (!isAtEnd() && (isAlphaNumeric(peek()) || peek() == '.')) {
            sb.append(advance());
        }

        String text = sb.toString();
        TokenType type = ANNOTATIONS.getOrDefault(text, TokenType.ANNOTATION_OTHER);
        return new Token(type, text, startLine, startCol, sourceFile);
    }

    private Token scanStringLiteral() {
        int startLine = line;
        int startCol = column;
        advance(); // consume opening quote

        StringBuilder sb = new StringBuilder();
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\\' && peekNext() != '\0') {
                advance(); // consume backslash
                char escaped = advance();
                if (escaped == 'n') sb.append('\n');
                else if (escaped == 't') sb.append('\t');
                else if (escaped == '"') sb.append('"');
                else if (escaped == '\\') sb.append('\\');
                else sb.append(escaped);
            } else {
                if (peek() == '\n') {
                    line++;
                    column = 0;
                }
                sb.append(advance());
            }
        }

        if (!isAtEnd() && peek() == '"') {
            advance(); // consume closing quote
        }

        return new Token(TokenType.STRING_LITERAL, sb.toString(), startLine, startCol, sourceFile);
    }

    private Token scanNumberLiteral() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && isDigit(peek())) {
            sb.append(advance());
        }

        if (!isAtEnd() && peek() == '.' && isDigit(peekNext())) {
            sb.append(advance()); // consume '.'
            while (!isAtEnd() && isDigit(peek())) {
                sb.append(advance());
            }
        }

        return new Token(TokenType.NUMBER_LITERAL, sb.toString(), startLine, startCol, sourceFile);
    }

    private Token scanIdentifierOrKeyword() {
        int startLine = line;
        int startCol = column;
        StringBuilder sb = new StringBuilder();

        while (!isAtEnd() && isAlphaNumeric(peek())) {
            sb.append(advance());
        }

        String text = sb.toString();
        if ("true".equals(text) || "false".equals(text)) {
            return new Token(TokenType.BOOLEAN_LITERAL, text, startLine, startCol, sourceFile);
        }

        if (KEYWORDS.containsKey(text)) {
            return new Token(KEYWORDS.get(text), text, startLine, startCol, sourceFile);
        }
        if (TYPES.containsKey(text)) {
            return new Token(TYPES.get(text), text, startLine, startCol, sourceFile);
        }

        return new Token(TokenType.IDENTIFIER, text, startLine, startCol, sourceFile);
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

    private void skipSingleLineComment() {
        while (!isAtEnd() && peek() != '\n') {
            advance();
        }
    }

    private void skipMultiLineComment() {
        advance(); // consume '/'
        advance(); // consume '*'
        while (!isAtEnd()) {
            if (peek() == '*' && peekNext() == '/') {
                advance(); // consume '*'
                advance(); // consume '/'
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

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
