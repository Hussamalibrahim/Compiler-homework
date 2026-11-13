package com.compiler.symboltable;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SymbolTable {
    private final Scope rootScope;
    private Scope currentScope;

    public SymbolTable() {
        this.rootScope = new Scope("global", null);
        this.currentScope = this.rootScope;
    }

    public void enterScope(String scopeName) {
        currentScope = new Scope(scopeName, currentScope);
    }

    public void exitScope() {
        if (currentScope.getParentScope() != null) {
            currentScope = currentScope.getParentScope();
        }
    }

    public void resetToRoot() {
        currentScope = rootScope;
    }

    public void addSymbol(Symbol symbol) {
        currentScope.define(symbol);
    }

    public void addSymbol(String name, SymbolType type, String sourceFile, int lineNumber, String extraDetails) {
        Symbol symbol = new Symbol(name, type, currentScope.getScopeName(), sourceFile, lineNumber, extraDetails);
        currentScope.define(symbol);
    }

    public Symbol lookup(String name) {
        return currentScope.lookup(name);
    }

    public List<Symbol> lookupAll(String name) {
        return currentScope.lookupAll(name);
    }

    public Symbol lookupCurrentScope(String name) {
        return currentScope.lookupCurrentScope(name);
    }

    public List<Symbol> queryByType(SymbolType type) {
        return rootScope.getAllSymbolsRecursive().stream()
                .filter(s -> s.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Symbol> queryByName(String name) {
        return rootScope.getAllSymbolsRecursive().stream()
                .filter(s -> s.getName().equals(name))
                .collect(Collectors.toList());
    }

    public List<Symbol> getAllSymbols() {
        return rootScope.getAllSymbolsRecursive();
    }

    public String printTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================================================\n");
        sb.append(String.format("%-22s | %-18s | %-16s | %-25s | %-9s | %s\n",
                "SYMBOL NAME", "TYPE", "SCOPE", "SOURCE FILE", "LINE", "DETAILS"));
        sb.append("------------------------------------------------------------------------------------------------------------------------\n");

        List<Symbol> all = getAllSymbols();
        if (all.isEmpty()) {
            sb.append(" (Symbol table is empty)\n");
        } else {
            for (Symbol s : all) {
                sb.append(s.toString()).append("\n");
            }
        }
        sb.append("========================================================================================================================\n");
        sb.append("Total Symbols: ").append(all.size()).append("\n");
        return sb.toString();
    }
}
