package com.compiler.lexer;

public enum TokenType {
    // Java Keywords & Modifiers
    KEYWORD_PACKAGE,
    KEYWORD_IMPORT,
    KEYWORD_PUBLIC,
    KEYWORD_PRIVATE,
    KEYWORD_PROTECTED,
    KEYWORD_CLASS,
    KEYWORD_RETURN,
    KEYWORD_VOID,
    KEYWORD_NEW,
    KEYWORD_IF,
    KEYWORD_ELSE,
    KEYWORD_FOR,
    KEYWORD_WHILE,

    // Data types
    TYPE_STRING,
    TYPE_INT,
    TYPE_DOUBLE,
    TYPE_BOOLEAN,
    TYPE_LONG,
    TYPE_MODEL,
    TYPE_LIST,

    // Identifiers & Literals
    IDENTIFIER,
    STRING_LITERAL,
    NUMBER_LITERAL,
    BOOLEAN_LITERAL,

    // Spring Boot Annotations
    ANNOTATION_CONTROLLER,
    ANNOTATION_REST_CONTROLLER,
    ANNOTATION_GET_MAPPING,
    ANNOTATION_POST_MAPPING,
    ANNOTATION_PATH_VARIABLE,
    ANNOTATION_REQUEST_PARAM,
    ANNOTATION_MODEL_ATTRIBUTE,
    ANNOTATION_OTHER,

    // Operators & Punctuation
    AT,                 // @
    ASSIGN,             // =
    DOT,                // .
    COMMA,              // ,
    SEMICOLON,          // ;
    COLON,              // :
    LPAREN,             // (
    RPAREN,             // )
    LBRACE,             // {
    RBRACE,             // }
    LBRACKET,           // [
    RBRACKET,           // ]
    ARROW,              // ->

    // HTML Tokens
    HTML_DOCTYPE,
    HTML_TAG_OPEN_START,   // <tag
    HTML_TAG_CLOSE_START,  // </tag
    HTML_TAG_END,          // >
    HTML_TAG_SELF_CLOSE,   // />
    HTML_TAG_NAME,
    HTML_ATTR_NAME,
    HTML_ATTR_VALUE,
    HTML_TEXT,

    // Thymeleaf Specific
    TH_ATTR_TEXT,          // th:text
    TH_ATTR_EACH,          // th:each
    TH_ATTR_IF,            // th:if
    TH_ATTR_UNLESS,        // th:unless
    TH_ATTR_HREF,          // th:href
    TH_ATTR_SRC,           // th:src
    TH_ATTR_VALUE,         // th:value
    TH_ATTR_OBJECT,        // th:object
    TH_ATTR_ACTION,        // th:action
    TH_ATTR_OTHER,         // other th:*

    // Thymeleaf Expression Tokens
    TH_EXPR_VARIABLE,      // ${...}
    TH_EXPR_SELECTION,     // *{...}
    TH_EXPR_LINK,          // @{...}

    // CSS Tokens
    CSS_SELECTOR,
    CSS_PROPERTY,
    CSS_VALUE,

    // General
    COMMENT,
    EOF,
    UNKNOWN
}
