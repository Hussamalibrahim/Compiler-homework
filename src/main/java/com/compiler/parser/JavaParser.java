package com.compiler.parser;

import com.compiler.ast.java.*;
import com.compiler.lexer.Token;
import com.compiler.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class JavaParser {
    private final List<Token> tokens;
    private final String sourceFile;
    private int cursor = 0;

    public JavaParser(List<Token> tokens, String sourceFile) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public JavaParser(List<Token> tokens) {
        this(tokens, "JavaSource.java");
    }

    public ClassNode parse() throws ParserException {
        String packageName = "";

        // Parse optional package
        if (check(TokenType.KEYWORD_PACKAGE)) {
            advance(); // package
            StringBuilder pkg = new StringBuilder();
            while (!isAtEnd() && !check(TokenType.SEMICOLON)) {
                pkg.append(advance().getValue());
            }
            consume(TokenType.SEMICOLON, "Expected ';' after package declaration");
            packageName = pkg.toString();
        }

        // Parse imports
        while (check(TokenType.KEYWORD_IMPORT)) {
            advance(); // import
            while (!isAtEnd() && !check(TokenType.SEMICOLON)) {
                advance();
            }
            consume(TokenType.SEMICOLON, "Expected ';' after import statement");
        }

        // Parse class-level annotations
        List<AnnotationNode> classAnnotations = parseAnnotations();

        // Optional modifier: public, etc.
        while (check(TokenType.KEYWORD_PUBLIC) || check(TokenType.KEYWORD_PROTECTED) || check(TokenType.KEYWORD_PRIVATE)) {
            advance();
        }

        // Expect 'class'
        Token classToken = consume(TokenType.KEYWORD_CLASS, "Expected 'class' keyword");
        Token classNameToken = consume(TokenType.IDENTIFIER, "Expected class name identifier");
        String className = classNameToken.getValue();

        // Check if controller
        boolean isController = false;
        boolean isRestController = false;
        String basePath = "";

        for (AnnotationNode ann : classAnnotations) {
            String name = ann.getAnnotationName();
            if ("@Controller".equals(name)) {
                isController = true;
            } else if ("@RestController".equals(name)) {
                isController = true;
                isRestController = true;
            } else if ("@RequestMapping".equals(name)) {
                basePath = ann.getValue();
            }
        }

        ClassNode classNode;
        if (isController) {
            classNode = new ControllerNode(className, packageName, basePath, isRestController, classToken.getLine(), sourceFile);
        } else {
            classNode = new ClassNode(className, packageName, classToken.getLine(), sourceFile);
        }

        for (AnnotationNode ann : classAnnotations) {
            classNode.addAnnotation(ann);
        }

        // Parse class body: '{' ... '}'
        consume(TokenType.LBRACE, "Expected '{' to start class body");

        while (!isAtEnd() && !check(TokenType.RBRACE)) {
            List<AnnotationNode> memberAnnotations = parseAnnotations();

            // Field or Method
            while (check(TokenType.KEYWORD_PUBLIC) || check(TokenType.KEYWORD_PROTECTED) || check(TokenType.KEYWORD_PRIVATE)) {
                advance();
            }

            if (isAtEnd() || check(TokenType.RBRACE)) break;

            // Member type
            String memberType = parseTypeName();
            if (memberType.isEmpty()) {
                advance();
                continue;
            }

            if (isAtEnd() || check(TokenType.RBRACE)) break;

            // Member name
            Token nameToken = peek();
            if (nameToken.getType() == TokenType.IDENTIFIER) {
                advance();
                String memberName = nameToken.getValue();

                // If followed by '(', it's a method!
                if (check(TokenType.LPAREN)) {
                    MethodNode method = parseMethod(memberAnnotations, memberType, memberName, nameToken.getLine(), basePath);
                    classNode.addMethod(method);
                } else {
                    // Field declaration
                    String initVal = "";
                    if (check(TokenType.ASSIGN)) {
                        advance();
                        StringBuilder valBuilder = new StringBuilder();
                        while (!isAtEnd() && !check(TokenType.SEMICOLON)) {
                            valBuilder.append(advance().getValue());
                        }
                        initVal = valBuilder.toString().trim();
                    }
                    if (check(TokenType.SEMICOLON)) {
                        advance();
                    }
                    VariableNode field = new VariableNode(memberType, memberName, initVal, nameToken.getLine(), sourceFile);
                    classNode.addField(field);
                }
            } else if (check(TokenType.SEMICOLON)) {
                advance();
            } else {
                advance();
            }
        }

        consume(TokenType.RBRACE, "Expected '}' to close class body");
        return classNode;
    }

    private MethodNode parseMethod(List<AnnotationNode> annotations, String returnType, String methodName, int line, String controllerBasePath) throws ParserException {
        MethodNode method = new MethodNode(methodName, returnType, line, sourceFile);
        for (AnnotationNode ann : annotations) {
            method.addAnnotation(ann);
        }

        // Determine endpoint from annotations
        for (AnnotationNode ann : annotations) {
            String annName = ann.getAnnotationName();
            String path = ann.getValue();
            if (path.isEmpty()) path = "/";

            String fullPath = combinePaths(controllerBasePath, path);

            if ("@GetMapping".equals(annName)) {
                method.setEndpoint(new EndpointNode("GET", fullPath, ann.getLineNumber(), sourceFile));
            } else if ("@PostMapping".equals(annName)) {
                method.setEndpoint(new EndpointNode("POST", fullPath, ann.getLineNumber(), sourceFile));
            } else if ("@RequestMapping".equals(annName)) {
                method.setEndpoint(new EndpointNode("REQUEST", fullPath, ann.getLineNumber(), sourceFile));
            }
        }

        // Parameters: '(' [param, param] ')'
        consume(TokenType.LPAREN, "Expected '(' for method parameters");
        if (!check(TokenType.RPAREN)) {
            do {
                List<AnnotationNode> paramAnns = parseAnnotations();
                String paramType = parseTypeName();
                if (paramType.isEmpty()) break;

                Token paramNameToken = consume(TokenType.IDENTIFIER, "Expected parameter name");
                ParameterNode paramNode = new ParameterNode(paramType, paramNameToken.getValue(), paramNameToken.getLine(), sourceFile);
                for (AnnotationNode pAnn : paramAnns) {
                    paramNode.addAnnotation(pAnn);
                }
                method.addParameter(paramNode);

                if (check(TokenType.COMMA)) {
                    advance();
                } else {
                    break;
                }
            } while (!isAtEnd() && !check(TokenType.RPAREN));
        }
        consume(TokenType.RPAREN, "Expected ')' after method parameters");

        // Method Body: '{' ... '}'
        if (check(TokenType.LBRACE)) {
            advance();
            int braceCount = 1;

            while (!isAtEnd() && braceCount > 0) {
                if (check(TokenType.LBRACE)) {
                    braceCount++;
                    advance();
                    continue;
                }
                if (check(TokenType.RBRACE)) {
                    braceCount--;
                    if (braceCount == 0) {
                        advance();
                        break;
                    }
                    advance();
                    continue;
                }

                // Check for model.addAttribute("key", value)
                if (check(TokenType.IDENTIFIER) && "model".equals(peek().getValue())) {
                    Token modelToken = advance(); // 'model'
                    if (check(TokenType.DOT)) {
                        advance(); // '.'
                        if (check(TokenType.IDENTIFIER) && "addAttribute".equals(peek().getValue())) {
                            advance(); // 'addAttribute'
                            if (check(TokenType.LPAREN)) {
                                advance(); // '('
                                if (check(TokenType.STRING_LITERAL)) {
                                    Token attrNameToken = advance();
                                    String attrName = attrNameToken.getValue();
                                    if (check(TokenType.COMMA)) {
                                        advance(); // ','
                                        StringBuilder expr = new StringBuilder();
                                        int parenDepth = 1;
                                        while (!isAtEnd() && parenDepth > 0) {
                                            if (check(TokenType.LPAREN)) parenDepth++;
                                            else if (check(TokenType.RPAREN)) {
                                                parenDepth--;
                                                if (parenDepth == 0) break;
                                            }
                                            expr.append(advance().getValue()).append(" ");
                                        }
                                        if (check(TokenType.RPAREN)) advance(); // ')'
                                        if (check(TokenType.SEMICOLON)) advance(); // ';'

                                        method.addModelAttribute(new ModelAttributeNode(
                                                attrName, expr.toString().trim(), attrNameToken.getLine(), sourceFile));
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }

                // Check for return statement: return "templateName";
                if (check(TokenType.KEYWORD_RETURN)) {
                    Token retToken = advance();
                    StringBuilder retExpr = new StringBuilder();
                    while (!isAtEnd() && !check(TokenType.SEMICOLON) && !check(TokenType.RBRACE)) {
                        retExpr.append(advance().getValue());
                    }
                    if (check(TokenType.SEMICOLON)) advance();

                    method.setReturnStatement(new ReturnStatementNode(retExpr.toString().trim(), retToken.getLine(), sourceFile));
                    continue;
                }

                // Any other token in method body
                advance();
            }
        } else if (check(TokenType.SEMICOLON)) {
            advance(); // abstract/interface method
        }

        return method;
    }

    private List<AnnotationNode> parseAnnotations() {
        List<AnnotationNode> list = new ArrayList<>();
        while (!isAtEnd() && isAnnotationToken(peek())) {
            Token annToken = advance();
            String annName = annToken.getValue();
            String annValue = "";

            if (check(TokenType.LPAREN)) {
                advance(); // '('
                StringBuilder val = new StringBuilder();
                while (!isAtEnd() && !check(TokenType.RPAREN)) {
                    Token t = advance();
                    if (t.getType() == TokenType.STRING_LITERAL) {
                        val.append(t.getValue());
                    } else if (t.getType() == TokenType.IDENTIFIER || t.getType() == TokenType.ASSIGN) {
                        // ignore "value ="
                        if (!"value".equals(t.getValue()) && !"=".equals(t.getValue())) {
                            val.append(t.getValue());
                        }
                    }
                }
                if (check(TokenType.RPAREN)) {
                    advance(); // ')'
                }
                annValue = val.toString().trim();
            }

            list.add(new AnnotationNode(annName, annValue, annToken.getLine(), sourceFile));
        }
        return list;
    }

    private boolean isAnnotationToken(Token token) {
        if (token == null) return false;
        switch (token.getType()) {
            case ANNOTATION_CONTROLLER:
            case ANNOTATION_REST_CONTROLLER:
            case ANNOTATION_GET_MAPPING:
            case ANNOTATION_POST_MAPPING:
            case ANNOTATION_PATH_VARIABLE:
            case ANNOTATION_REQUEST_PARAM:
            case ANNOTATION_MODEL_ATTRIBUTE:
            case ANNOTATION_OTHER:
                return true;
            default:
                return token.getValue().startsWith("@");
        }
    }

    private String parseTypeName() {
        if (isAtEnd()) return "";
        Token t = peek();
        switch (t.getType()) {
            case TYPE_STRING:
            case TYPE_INT:
            case TYPE_DOUBLE:
            case TYPE_BOOLEAN:
            case TYPE_LONG:
            case TYPE_MODEL:
            case TYPE_LIST:
            case KEYWORD_VOID:
            case IDENTIFIER:
                advance();
                StringBuilder type = new StringBuilder(t.getValue());
                // Handle generics: List<Product>
                if (check(TokenType.UNKNOWN) && "<".equals(peek().getValue())) {
                    advance();
                    type.append("<");
                    while (!isAtEnd() && !(check(TokenType.UNKNOWN) && ">".equals(peek().getValue()))) {
                        type.append(advance().getValue());
                    }
                    if (!isAtEnd()) {
                        type.append(advance().getValue()); // '>'
                    }
                }
                // Handle array []
                if (check(TokenType.LBRACKET)) {
                    advance();
                    if (check(TokenType.RBRACKET)) advance();
                    type.append("[]");
                }
                return type.toString();
            default:
                return "";
        }
    }

    private String combinePaths(String base, String sub) {
        if (base == null || base.isEmpty()) {
            return sub.startsWith("/") ? sub : "/" + sub;
        }
        if (!base.startsWith("/")) base = "/" + base;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!sub.startsWith("/")) sub = "/" + sub;
        if ("/".equals(sub)) return base;
        return base + sub;
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

    private Token consume(TokenType type, String message) throws ParserException {
        if (check(type)) return advance();
        Token current = peek();
        throw new ParserException(message + ", found: '" + current.getValue() + "' (" + current.getType() + ")",
                current.getLine(), current.getColumn(), sourceFile);
    }
}
