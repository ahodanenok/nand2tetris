package ahodanenok.nand2tetris.jack.symbol;

public class SymbolDef {

    public final String valueType;
    public final SymbolType symbolType;
    public final int index;

    public SymbolDef(String valueType, SymbolType symbolType, int index) {
        this.valueType = valueType;
        this.symbolType = symbolType;
        this.index = index;
    }
}
