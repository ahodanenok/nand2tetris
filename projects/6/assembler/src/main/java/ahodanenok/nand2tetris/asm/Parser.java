package ahodanenok.nand2tetris.asm;

public class Parser {

    private final String code;
    private int pos;
    private InstructionType instType;
    private int number;
    private String symbol;
    private String dest;
    private String comp;
    private String jump;

    public Parser(String code) {
        this.code = code;
        this.pos = 0;
        resetFields();
    }

    public boolean advance() {
        resetFields();
        return parseInstruction();
    }

    public InstructionType instType() {
        return instType;
    }

    public int number() {
        return number;
    }

    public String symbol() {
        return symbol;
    }

    public String dest() {
        return dest;
    }

    public String comp() {
        return comp;
    }

    public String jump() {
        return jump;
    }

    private void resetFields() {
        instType = null;
        number = -1;
        symbol = null;
        dest = null;
        comp = null;
        jump = null;
    }

    private boolean parseInstruction() {
        while (hasMoreChars()) {
            char ch = peekChar();
            if (ch == ' ') {
                readChar();
            } else if (isEOL(ch)) {
                readChar();
            } else if (ch == '/') {
                readChar(); // /
                readChar(); // /
                while (hasMoreChars() && !isEOL(peekChar())) {
                    readChar();
                }
            } else if (ch == '@') {
                parseA();
                return true;
            } else if (ch == '(') {
                parseL();
                return true;
            } else {
                parseC();
                return true;
            }
        }

        return false;
    }

    private void parseA() {
        instType = InstructionType.A;

        readChar(); // @
        char ch = peekChar();
        if (isDigit(ch)) {
            String s = "";
            while (hasMoreChars() && isDigit(peekChar())) {
                s += readChar();
            }

            number = Integer.parseInt(s);
        } else {
            parseSymbol();
        }
    }

    private void parseL() {
        instType = InstructionType.L;
        readChar(); // (
        parseSymbol();
        readChar(); // )
    }

    private void parseC() {
        instType = InstructionType.C;

        char ch;
        String s = "";
        while (hasMoreChars()) {
            ch = readChar();
            if (ch == ' ') {
                break;
            } else if (isEOL(ch)) {
                break;
            } else if (ch == '/') {
                break;
            } else if (ch == '=') {
                dest = s;
                s = "";
            } else if (ch == ';') {
                comp = s;
                s = "";
            } else {
                s += ch;
            }
        }

        if (comp != null) {
            jump = s;
        } else {
            comp = s;
        }
    }

    private void parseSymbol() {
        String s = "";
        while (hasMoreChars() && isSymbolChar(peekChar())) {
            s += readChar();
        }

        symbol = s;
    }

    private boolean isEOL(char ch) {
        return ch == '\n' || ch == '\r';
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isSymbolChar(char ch) {
        return (ch >= 'a' && ch <= 'z')
            || (ch >= 'A' && ch <= 'Z')
            || ch =='_' || ch == '.' || ch == '$' || ch == ':'
            || isDigit(ch);
    }

    private boolean hasMoreChars() {
        return pos < code.length();
    }

    private char readChar() {
        return code.charAt(pos++);
    }

    private char peekChar() {
        return code.charAt(pos);
    }
}
