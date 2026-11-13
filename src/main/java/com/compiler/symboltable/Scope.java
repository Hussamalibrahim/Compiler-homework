package com.compiler.symboltable;

import java.util.*;

public class Scope {
    private final String scopeName;
    private final Scope parentScope;
    private final List<Scope> childScopes = new ArrayList<>();
    private final Map<String, List<Symbol>> symbols = new LinkedHashMap<>();

    public Scope(String scopeName, Scope parentScope) {
        this.scopeName = scopeName;
        this.parentScope = parentScope;
        if (parentScope != null) {
            parentScope.childScopes.add(this);
        }
    }

    public String getScopeName() {
        return scopeName;
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public List<Scope> getChildScopes() {
        return Collections.unmodifiableList(childScopes);
    }

    public void define(Symbol symbol) {
        if (symbol == null) return;
        symbols.computeIfAbsent(symbol.getName(), k -> new ArrayList<>()).add(symbol);
    }

    public Symbol lookup(String name) {
        List<Symbol> localList = symbols.get(name);
        if (localList != null && !localList.isEmpty()) {
            return localList.get(0);
        }
        if (parentScope != null) {
            return parentScope.lookup(name);
        }
        return null;
    }

    public List<Symbol> lookupAll(String name) {
        List<Symbol> result = new ArrayList<>();
        List<Symbol> localList = symbols.get(name);
        if (localList != null) {
            result.addAll(localList);
        }
        if (parentScope != null) {
            result.addAll(parentScope.lookupAll(name));
        }
        return result;
    }

    public Symbol lookupCurrentScope(String name) {
        List<Symbol> localList = symbols.get(name);
        return (localList != null && !localList.isEmpty()) ? localList.get(0) : null;
    }

    public List<Symbol> getAllSymbolsInScope() {
        List<Symbol> all = new ArrayList<>();
        for (List<Symbol> list : symbols.values()) {
            all.addAll(list);
        }
        return all;
    }

    public List<Symbol> getAllSymbolsRecursive() {
        List<Symbol> all = new ArrayList<>(getAllSymbolsInScope());
        for (Scope child : childScopes) {
            all.addAll(child.getAllSymbolsRecursive());
        }
        return all;
    }
}
