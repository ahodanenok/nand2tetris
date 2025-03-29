package ahodanenok.nand2tetris.asm;

import java.util.Map;
import java.util.HashMap;

public class SymbolTable {

    private final Map<String, Integer> map = new HashMap<>();

    public void assign(String symbol, int value) {
        if (map.containsKey(symbol)) {
            throw new IllegalStateException(
                String.format("Symbol already used: '%s'", symbol));
        }

        map.put(symbol, value);
    }

    public int lookup(String symbol) {
        if (!map.containsKey(symbol)) {
            throw new IllegalStateException(
                String.format("Unknown symbol: '%s'", symbol));
        }

        return map.get(symbol);
    }

    public boolean exists(String symbol) {
        return map.containsKey(symbol);
    }
}
