package ahodanenok.nand2tetris.jack;

import java.util.HashMap;

public class JackTokenizer {

    private static final HashMap<String, TokenType> KEYWORDS;
    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("class", TokenType.CLASS);
        KEYWORDS.put("constructor", TokenType.CONSTRUCTOR);
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("method", TokenType.METHOD);
        KEYWORDS.put("field", TokenType.FIELD);
        KEYWORDS.put("static", TokenType.STATIC);
        KEYWORDS.put("var", TokenType.VAR);
        KEYWORDS.put("int", TokenType.INT);
        KEYWORDS.put("char", TokenType.CHAR);
        KEYWORDS.put("boolean", TokenType.BOOLEAN);
        KEYWORDS.put("void", TokenType.VOID);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("null", TokenType.NULL);
        KEYWORDS.put("this", TokenType.THIS);
        KEYWORDS.put("let", TokenType.LET);
        KEYWORDS.put("do", TokenType.DO);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("return", TokenType.RETURN);
    }

    private final String code;
    private int pos;

    private TokenType tokenType;
    private String tokenValue;
    private int integer;
    private String string;

    private StringBuilder buf;

    public JackTokenizer(String code) {
        this.code = code;
        this.buf = new StringBuilder();
    }

    public TokenType tokenType() {
        return tokenType;
    }

    public String tokenValue() {
        return tokenValue;
    }

    public int integer() {
        return integer;
    }

    public String string() {
        return string;
    }

    public boolean advance() {
        reset();
        while (hasMoreChars()) {
            char ch = peekChar();
            if (isWhitespace(ch) || isNewLine(ch)) {
                readChar(); // skip
            } else if (ch == '{') {
                tokenType = TokenType.LEFT_BRACE;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '}') {
                tokenType = TokenType.RIGHT_BRACE;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '(') {
                tokenType = TokenType.LEFT_PAREN;
                tokenValue = String.valueOf(readChar());
            } else if (ch == ')') {
                tokenType = TokenType.RIGHT_PAREN;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '[') {
                tokenType = TokenType.LEFT_BRACKET;
                tokenValue = String.valueOf(readChar());
            } else if (ch == ']') {
                tokenType = TokenType.RIGHT_BRACKET;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '.') {
                tokenType = TokenType.DOT;
                tokenValue = String.valueOf(readChar());
            } else if (ch == ',') {
                tokenType = TokenType.COMMA;
                tokenValue = String.valueOf(readChar());
            } else if (ch == ';') {
                tokenType = TokenType.SEMICOLON;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '+') {
                tokenType = TokenType.PLUS;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '-') {
                tokenType = TokenType.MINUS;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '*') {
                tokenType = TokenType.STAR;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '/') {
                readChar(); // skip
                if (hasMoreChars() && peekChar() == '/') {
                    while (hasMoreChars() && !isNewLine(peekChar())) {
                        readChar(); // skip
                    }
                } else if (hasMoreChars() && peekChar() == '*') {
                    readChar(); // skip *
                    while (hasMoreChars()) {
                        ch = readChar(); // skip
                        if (ch == '*') {
                            if (!hasMoreChars()) {
                                throw new IllegalStateException("Unclosed block comment");
                            } else if (peekChar() == '/') {
                                readChar(); // skip /
                                break;
                            }
                        }
                    }
                } else {
                    tokenType = TokenType.SLASH;
                    tokenValue = "/";
                }
            } else if (ch == '&') {
                tokenType = TokenType.AMPERSAND;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '|') {
                tokenType = TokenType.VBAR;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '<') {
                tokenType = TokenType.LEFT_ANGLE;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '>') {
                tokenType = TokenType.RIGHT_ANGLE;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '=') {
                tokenType = TokenType.EQUAL;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '~') {
                tokenType = TokenType.TILDE;
                tokenValue = String.valueOf(readChar());
            } else if (ch == '"') {
                tokenType = TokenType.STRING;
                tokenValue = readString();
                string = tokenValue.substring(1, tokenValue.length() - 1);
            } else if (isDigit(ch)) {
                tokenType = TokenType.INTEGER;
                tokenValue = readInteger();
                integer = parseInteger(tokenValue);
            } else {
                String identifier = readIdentifier();
                if (KEYWORDS.containsKey(identifier)) {
                    tokenType = KEYWORDS.get(identifier);
                } else {
                    tokenType = TokenType.IDENTIFIER;
                }
                tokenValue = identifier;
            }

            if (tokenType != null) {
                return true;
            }
        }

        return false;
    }

    private void reset() {
        this.tokenType = null;
        this.tokenValue = null;
        this.string = null;
        this.integer = -1;
        this.buf.setLength(0);
    }

    private boolean isNewLine(char ch) {
        return ch == '\n' || ch == '\r';
    }

    private boolean isWhitespace(char ch) {
        return ch == ' ' || ch == '\t';
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return ch >= 'a' && ch <= 'z'
            || ch >= 'A' && ch <= 'Z';
    }

    private String readIdentifier() {
        while (hasMoreChars()) {
            char ch = peekChar();
            if (ch == '_' || isDigit(ch) || isAlpha(ch)) {
                buf.append(readChar());
            } else {
                break;
            }
        }

        if (buf.isEmpty()) {
            throw new IllegalStateException("Expected identifier");
        }

        return buf.toString();
    }

    private String readString() {
        buf.append(readChar()); // "
        while (hasMoreChars() && peekChar() != '\"') {
            char ch = readChar();
            if (ch == '\\') {
                if (!hasMoreChars()) {
                    throw new IllegalStateException("Unterminated string");
                }

                ch = readChar();
                if (ch == '"') {
                    buf.append('"');
                } else if (ch == 'n') {
                    buf.append('\n');
                } else if (ch == 't') {
                    buf.append('\t');
                } else if (ch == '\\') {
                    buf.append('\\');
                } else {
                    throw new IllegalStateException("Unsupported escape: \\" + ch);
                }
            } else {
                buf.append(ch);
            }
        }

        if (!hasMoreChars() || peekChar() != '\"') {
            throw new IllegalStateException("Unterminated string");
        }
        buf.append(readChar()); // "

        return buf.toString();
    }

    private String readInteger() {
        while (hasMoreChars() && isDigit(peekChar())) {
            buf.append(readChar());
        }

        return buf.toString();
    }

    private int parseInteger(String str) {
        int n;
        try {
            n = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Illegal number literal");
        }

        if (n < 0 || n > 32767) {
            throw new IllegalStateException("Integer is out of range");
        }

        return n;
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
