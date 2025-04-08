package ahodanenok.nand2tetris.vm.translator;

public class VmParser {

    private final String code;
    private int pos;

    private VmCommand command;
    private VmSegment segment;
    private int number;
    private String label;

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

    public int number() {
        return number;
    }

    public String label() {
        return label;
    }

    private void resetFields() {
        this.command = null;
        this.segment = null;
        this.number = -1;
        this.label = null;
    }

    private boolean parseCommand() {
        while (nextLine());
        if (!hasMoreChars()) {
            return false;
        }

        String cmdToken = readKeyword();
        if (cmdToken.equals("push")) {
            command = VmCommand.PUSH;
            segment = parseSegment(readKeyword());
            number = parseNumber(readNumber());
        } else if (cmdToken.equals("pop")) {
            command = VmCommand.POP;
            segment = parseSegment(readKeyword());
            number = parseNumber(readNumber());
        } else if (cmdToken.equals("add")) {
            command = VmCommand.ADD;
        } else if (cmdToken.equals("sub")) {
            command = VmCommand.SUB;
        } else if (cmdToken.equals("neg")) {
            command = VmCommand.NEG;
        } else if (cmdToken.equals("eq")) {
            command = VmCommand.EQ;
        } else if (cmdToken.equals("gt")) {
            command = VmCommand.GT;
        } else if (cmdToken.equals("lt")) {
            command = VmCommand.LT;
        } else if (cmdToken.equals("and")) {
            command = VmCommand.AND;
        } else if (cmdToken.equals("or")) {
            command = VmCommand.OR;
        } else if (cmdToken.equals("not")) {
            command = VmCommand.NOT;
        } else if (cmdToken.equals("label")) {
            command = VmCommand.LABEL;
            label = readKeyword();
        } else if (cmdToken.equals("goto")) {
            command = VmCommand.GOTO;
            label = readKeyword();
        } else if (cmdToken.equals("if-goto")) {
            command = VmCommand.IF_GOTO;
            label = readKeyword();
        } else if (cmdToken.equals("function")) {
            command = VmCommand.FUNCTION;
            label = readKeyword();
            number = parseNumber(readNumber());
        } else if (cmdToken.equals("call")) {
            command = VmCommand.CALL;
            label = readKeyword();
            number = parseNumber(readNumber());
        } else if (cmdToken.equals("return")) {
            command = VmCommand.RETURN;
        } else {
            throw new IllegalStateException(String.format(
                "Unknown command: '%s'", cmdToken));
        }

        if (hasMoreChars() && !nextLine()) {
            throw new IllegalStateException("Expected new line");
        }

        return true;
    }

    private String readKeyword() {
        while (hasMoreChars() && isWhitespace(peekChar())) {
            readChar();
        }

        String token = "";
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isDigit(ch) && token.length() > 0) {
                token += readChar();
            } else if (isAlpha(ch)
                    || ch == '_' || ch == '.' || ch == ':' || ch == '-') {
                token += readChar();
            } else if (isWhitespace(ch)) {
                break;
            } else if (isNewLine(ch)) {
                break;
            } else {
                throw unexpectedChar(ch);
            }
        }

        if (token.isEmpty()) {
            throw new IllegalStateException("Expected keyword");
        }

        return token;
    }

    private String readNumber() {
        while (hasMoreChars() && isWhitespace(peekChar())) {
            readChar();
        }

        String token = "";
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isDigit(ch)) {
                token += readChar();
            } else if (isWhitespace(ch)) {
                break;
            } else if (isNewLine(ch)) {
                break;
            } else {
                throw unexpectedChar(ch);
            }
        }

        if (token.isEmpty()) {
            throw new IllegalStateException("Expected number");
        }

        return token;
    }

    private boolean nextLine() {
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isWhitespace(ch)) {
                readChar();
            } else if (ch == '/') {
                readChar();
                if (!hasMoreChars() || peekChar() != '/') {
                    throw unexpectedChar(peekChar());
                }
                while (hasMoreChars() && !isNewLine(peekChar())) {
                    readChar();
                }
            } else {
                break;
            }
        }

        if (hasMoreChars() && isNewLine(peekChar())) {
            readChar();
            return true;
        } else {
            return false;
        }
    }

    private VmSegment parseSegment(String token) {
        for (VmSegment segment : VmSegment.values()) {
            if (segment.name().equalsIgnoreCase(token)) {
                return segment;
            }
        }

        throw new IllegalStateException("Unknown segment: " + token);
    }

    private int parseNumber(String token) {
        int number;
        try {
            number = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(String.format(
                "Invalid number: '%s'", token));
        }

        if (number < 0 || number > 32767) {
            throw new IllegalStateException(String.format(
                "Number out of range: '%s'", number));
        }

        return number;
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

    private boolean isAlpha(char ch) {
        return ch >= 'a' && ch <= 'z'
            || ch >= 'A' && ch <= 'Z';
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    // private boolean isAlphanum(char ch) {
    //     return isAlpha(ch)
    //         || ch >= '0' && ch <= '9'
    //         || ch == '/';
    // }

    private boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t';
    }

    private IllegalStateException unexpectedChar(char ch) {
        return new IllegalStateException(String.format(
            "Unexpected character: '%s'", ch));
    }
}
