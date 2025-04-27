package ahodanenok.nand2tetris.jack.symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private final Map<String, SymbolDef> symbols = new HashMap<>();

    public void add(String name, String valueType, SymbolType symbolType, int index) {
        if (symbols.containsKey(name)) {
            throw new IllegalStateException(
                String.format("Name '%s' already exists", name));
        }

        symbols.put(name, new SymbolDef(valueType, symbolType, index));
    }

    public boolean contains(String name) {
        return symbols.containsKey(name);
    }

    public SymbolDef get(String name) {
        return symbols.get(name);
    }
}
