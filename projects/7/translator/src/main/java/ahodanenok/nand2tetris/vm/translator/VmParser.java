package ahodanenok.nand2tetris.vm.translator;

public class VmParser {

    private final String code;
    private int pos;

    private VmCommand command;
    private VmSegment segment;
    private int index;

    public VmParser(String code) {
        this.code = code;
    }

    public boolean advance() {
        resetFields();
        return parseCommand();
    }

    public VmCommand command() {
        return command;
    }

    public VmSegment segment() {
        return segment;
    }

    public int index() {
        return index;
    }

    private void resetFields() {
        this.command = null;
        this.segment = null;
        this.index = -1;
    }

    private boolean parseCommand() {
        while (hasMoreChars()) {
            while (hasMoreChars() && isNewLine(peekChar())) {
                readChar();
            }

            String token = readToken();
            if (token.isEmpty()) {
                continue;
            } else if (token.startsWith("//")) {
                while (hasMoreChars() && !isNewLine(peekChar())) {
                    readChar();
                }
            } else if (token.equals("push")) {
                command = VmCommand.PUSH;
                segment = parseSegment(readToken());
                index = Integer.parseInt(readToken());
            } else if (token.equals("pop")) {
                command = VmCommand.POP;
                segment = parseSegment(readToken());
                index = Integer.parseInt(readToken());
            } else if (token.equals("add")) {
                command = VmCommand.ADD;
            } else if (token.equals("sub")) {
                command = VmCommand.SUB;
            } else if (token.equals("neg")) {
                command = VmCommand.NEG;
            } else if (token.equals("eq")) {
                command = VmCommand.EQ;
            } else if (token.equals("gt")) {
                command = VmCommand.GT;
            } else if (token.equals("lt")) {
                command = VmCommand.LT;
            } else if (token.equals("and")) {
                command = VmCommand.AND;
            } else if (token.equals("or")) {
                command = VmCommand.OR;
            } else if (token.equals("not")) {
                command = VmCommand.NOT;
            } else {
                throw new IllegalStateException(String.format("Unknown token: '%s'", token));
            }

            while (hasMoreChars() && isWhitespace(peekChar())) {
                readChar();
            }
            if (hasMoreChars() && !isNewLine(peekChar())) {
                throw new IllegalStateException("Expected new line");
            }

            if (command != null) {
                return true;
            }
        }

        return false;
    }

    // private String readToken() {
    //     while (hasMoreChars() && isWhitespace(peekChar())) {
    //         readChar();
    //     }

    //     String token = "";
    //     while (hasMoreChars() && isAlphanum(peekChar())) {
    //         token += readChar();
    //     }

    //     return token;
    // }

    private String readToken() {
        String token = "";
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isWhitespace(ch)) {
                readChar();
                if (!token.isEmpty()) {
                    break;
                }
            } else if (isNewLine(ch)) {
                break;
            } else if (isAlphanum(ch)) {
                token += readChar();
            } else {
                throw new IllegalStateException(String.format("Unknown symbol: '%s'", ch));
            }
        }

        return token;
    }

    private VmSegment parseSegment(String name) {
        for (VmSegment segment : VmSegment.values()) {
            if (segment.name().equalsIgnoreCase(name)) {
                return segment;
            }
        }

        throw new IllegalStateException("Unknown segment: " + name);
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

    private boolean isNewLine(char ch) {
        return ch == '\n' || ch == '\r';
    }

    private boolean isAlphanum(char ch) {
        return ch >= 'a' && ch <= 'z'
            || ch >= '0' && ch <= '9'
            || ch == '/';
    }

    private boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t';
    }
}
