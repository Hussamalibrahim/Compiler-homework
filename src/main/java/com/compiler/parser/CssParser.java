package com.compiler.parser;

import com.compiler.ast.css.CssDeclarationNode;
import com.compiler.ast.css.CssRuleNode;
import com.compiler.ast.css.CssSelectorNode;
import com.compiler.ast.css.StyleSheetNode;
import com.compiler.lexer.CssLexer;
import com.compiler.lexer.Token;
import com.compiler.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class CssParser {
    private final List<Token> tokens;
    private final String sheetName;
    private final String sourceFile;
    private int cursor = 0;

    public CssParser(List<Token> tokens, String sheetName, String sourceFile) {
        this.tokens = tokens != null ? tokens : new ArrayList<>();
        this.sheetName = sheetName != null ? sheetName : "styles.css";
        this.sourceFile = sourceFile != null ? sourceFile : "";
    }

    public CssParser(String cssSource, String sheetName) {
        this(new CssLexer(cssSource, sheetName).tokenize(), sheetName, sheetName);
    }

    public StyleSheetNode parse() throws ParserException {
        StyleSheetNode sheet = new StyleSheetNode(sheetName, 1, sourceFile);

        while (!isAtEnd()) {
            if (check(TokenType.CSS_SELECTOR)) {
                Token selToken = advance();
                CssSelectorNode selectorNode = new CssSelectorNode(selToken.getValue(), selToken.getLine(), sourceFile);
                CssRuleNode ruleNode = new CssRuleNode(selectorNode, selToken.getLine(), sourceFile);

                if (check(TokenType.LBRACE)) {
                    advance(); // '{'
                    while (!isAtEnd() && !check(TokenType.RBRACE)) {
                        if (check(TokenType.CSS_PROPERTY)) {
                            Token propToken = advance();
                            String prop = propToken.getValue();
                            String val = "";

                            if (check(TokenType.COLON)) {
                                advance(); // ':'
                            }
                            if (check(TokenType.CSS_VALUE)) {
                                val = advance().getValue();
                            }
                            if (check(TokenType.SEMICOLON)) {
                                advance(); // ';'
                            }

                            ruleNode.addDeclaration(new CssDeclarationNode(prop, val, propToken.getLine(), sourceFile));
                        } else {
                            advance();
                        }
                    }
                    if (check(TokenType.RBRACE)) {
                        advance(); // '}'
                    }
                }
                sheet.addRule(ruleNode);
            } else {
                advance();
            }
        }

        return sheet;
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
