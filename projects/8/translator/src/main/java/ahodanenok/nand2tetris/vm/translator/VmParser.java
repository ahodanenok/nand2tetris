package ahodanenok.nand2tetris.vm.translator;

public class VmParser {

    private final String code;
    private int pos;

    private VmCommand command;
    private VmSegment segment;
    private int index;
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

    public int index() {
        return index;
    }

    public String label() {
        return label;
    }

    private void resetFields() {
        this.command = null;
        this.segment = null;
        this.index = -1;
        this.label = null;
    }

    // private boolean parseCommand() {
    //     while (hasMoreChars()) {
    //         while (hasMoreChars() && isNewLine(peekChar())) {
    //             readChar();
    //         }

    //         String token = readToken();
    //         if (token.isEmpty()) {
    //             continue;
    //         } else if (token.startsWith("//")) {
    //             while (hasMoreChars() && !isNewLine(peekChar())) {
    //                 readChar();
    //             }
    //         } else if (token.equals("push")) {
    //             command = VmCommand.PUSH;
    //             segment = parseSegment(readToken());
    //             index = Integer.parseInt(readToken());
    //         } else if (token.equals("pop")) {
    //             command = VmCommand.POP;
    //             segment = parseSegment(readToken());
    //             index = Integer.parseInt(readToken());
    //         } else if (token.equals("add")) {
    //             command = VmCommand.ADD;
    //         } else if (token.equals("sub")) {
    //             command = VmCommand.SUB;
    //         } else if (token.equals("neg")) {
    //             command = VmCommand.NEG;
    //         } else if (token.equals("eq")) {
    //             command = VmCommand.EQ;
    //         } else if (token.equals("gt")) {
    //             command = VmCommand.GT;
    //         } else if (token.equals("lt")) {
    //             command = VmCommand.LT;
    //         } else if (token.equals("and")) {
    //             command = VmCommand.AND;
    //         } else if (token.equals("or")) {
    //             command = VmCommand.OR;
    //         } else if (token.equals("not")) {
    //             command = VmCommand.NOT;
    //         } else {
    //             throw new IllegalStateException(String.format("Unknown token: '%s'", token));
    //         }

    //         while (hasMoreChars() && isWhitespace(peekChar())) {
    //             readChar();
    //         }
    //         if (hasMoreChars() && !isNewLine(peekChar())) {
    //             throw new IllegalStateException("Expected new line");
    //         }

    //         if (command != null) {
    //             return true;
    //         }
    //     }

    //     return false;
    // }

    private boolean parseCommand() {
        while (nextLine());
        if (!hasMoreChars()) {
            return false;
        }

        String cmdToken = readKeyword();
        if (cmdToken.equals("push")) {
            command = VmCommand.PUSH;
            segment = parseSegment(readKeyword());
            index = parseIndex(readIndex());
        } else if (cmdToken.equals("pop")) {
            command = VmCommand.POP;
            segment = parseSegment(readKeyword());
            index = parseIndex(readIndex());
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

        String command = "";
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isDigit(ch) && command.length() > 0) {
                command += readChar();
            } else if (isAlpha(ch)
                    || ch == '_' || ch == '.' || ch == ':' || ch == '-') {
                command += readChar();
            } else if (isWhitespace(ch)) {
                break;
            } else if (isNewLine(ch)) {
                break;
            } else {
                throw unexpectedChar(ch);
            }
        }

        if (command.isEmpty()) {
            throw new IllegalStateException("Expected keyword");
        }

        return command;
    }

    private String readIndex() {
        while (hasMoreChars() && isWhitespace(peekChar())) {
            readChar();
        }

        String index = "";
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isDigit(ch)) {
                index += readChar();
            } else if (isWhitespace(ch)) {
                break;
            } else if (isNewLine(ch)) {
                break;
            } else {
                throw unexpectedChar(ch);
            }
        }

        if (index.isEmpty()) {
            throw new IllegalStateException("Expected index");
        }

        return index;
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

    // private String readToken() {
    //     String token = "";
    //     while (hasMoreChars()) {
    //         char ch = peekChar();
    //         if (isWhitespace(ch)) {
    //             readChar();
    //             if (!token.isEmpty()) {
    //                 break;
    //             }
    //         } else if (isNewLine(ch)) {
    //             break;
    //         } else if (isAlphanum(ch)) {
    //             token += readChar();
    //         } else {
    //             throw new IllegalStateException(String.format("Unknown symbol: '%s'", ch));
    //         }
    //     }

    //     return token;
    // }

    private VmSegment parseSegment(String token) {
        for (VmSegment segment : VmSegment.values()) {
            if (segment.name().equalsIgnoreCase(token)) {
                return segment;
            }
        }

        throw new IllegalStateException("Unknown segment: " + token);
    }

    private int parseIndex(String token) {
        int index;
        try {
            index = Integer.parseInt(token);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(String.format(
                "Invalid index: '%s'", token));
        }

        if (index < 0 || index > 32767) {
            throw new IllegalStateException(String.format(
                "Index out of range: '%s'", index));
        }

        return index;
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
