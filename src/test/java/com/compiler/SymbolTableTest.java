package com.compiler;

import com.compiler.symboltable.Symbol;
import com.compiler.symboltable.SymbolTable;
import com.compiler.symboltable.SymbolType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SymbolTableTest {

    @Test
    public void testSymbolTableScopesAndLookup() {
        SymbolTable table = new SymbolTable();

        // Global scope
        table.addSymbol("ProductController", SymbolType.CONTROLLER, "ProductController.java", 10, "Spring Controller");
        assertNotNull(table.lookup("ProductController"));

        // Enter method scope
        table.enterScope("method:listProducts");
        table.addSymbol("products", SymbolType.MODEL_ATTRIBUTE, "ProductController.java", 15, "Model attribute");

        // Local lookup
        assertNotNull(table.lookup("products"));
        // Parent lookup
        assertNotNull(table.lookup("ProductController"));

        // Query by type
        List<Symbol> controllers = table.queryByType(SymbolType.CONTROLLER);
        assertEquals(1, controllers.size());

        List<Symbol> modelAttrs = table.queryByType(SymbolType.MODEL_ATTRIBUTE);
        assertEquals(1, modelAttrs.size());

        // Exit scope
        table.exitScope();
        assertNull(table.lookupCurrentScope("products"));
    }
}
