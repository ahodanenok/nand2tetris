package ahodanenok.nand2tetris.jack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JackCompiler {

    private static final String JACK_FILE_EXT = ".jack";
    private static final String XML_FILE_EXT = ".xml";

    public static void main(String... args) throws Exception {
        Path sourcePath = Paths.get(args[0]);
        String sourcePathName = sourcePath.getFileName().toString();
        if (!sourcePathName.endsWith(JACK_FILE_EXT)) {
            System.out.println("Not a jack file");
            System.exit(-1);
            return;
        }

        String code = new String(Files.readAllBytes(sourcePath), "UTF-8");
        Path targetPath = sourcePath.toAbsolutePath().getParent().resolve(
            sourcePathName.substring(
                0, sourcePathName.length() - JACK_FILE_EXT.length()) + XML_FILE_EXT);
        try (BufferedWriter out = Files.newBufferedWriter(targetPath)) {
            JackCompiler compiler = new JackCompiler(code, out);
            compiler.compile();
        }
    }

    private final JackTokenizer tokenizer;
    private final Writer out;
    private int indent;

    public JackCompiler(String code, Writer out) {
        this.tokenizer = new JackTokenizer(code);
        this.out = out;
    }

    public void compile() throws IOException {
        while (tokenizer.advance()) {
            switch (tokenizer.tokenType()) {
                case CLASS -> compileClass();
                default -> throw new IllegalStateException(
                    "Unexpected token: " + tokenizer.tokenValue());
            }
        }
    }

    private void compileClass() throws IOException {
        writeStart("class");
        process(TokenType.CLASS, "keyword");
        process(TokenType.IDENTIFIER, "identifier");
        process(TokenType.LEFT_BRACE, "symbol");
        while (tokenizer.tokenType() == TokenType.STATIC
                || tokenizer.tokenType() == TokenType.FIELD) {
            writeStart("classVarDec");
            if (tokenizer.tokenType() == TokenType.STATIC) {
                process(TokenType.STATIC, "keyword");
            } else {
                process(TokenType.FIELD, "keyword");
            }

            processType();
            process(TokenType.IDENTIFIER, "identifier");
            while (tokenizer.tokenType() == TokenType.COMMA) {
                process(TokenType.COMMA, "symbol");
                process(TokenType.IDENTIFIER, "identifier");
            }
            process(TokenType.SEMICOLON, "symbol");
            writeEnd("classVarDec");
        }

        while (tokenizer.tokenType() == TokenType.CONSTRUCTOR
                || tokenizer.tokenType() == TokenType.FUNCTION
                || tokenizer.tokenType() == TokenType.METHOD) {
            writeStart("subroutineDec");
            switch (tokenizer.tokenType()) {
                case CONSTRUCTOR -> process(TokenType.CONSTRUCTOR, "keyword");
                case FUNCTION -> process(TokenType.FUNCTION, "keyword");
                case METHOD -> process(TokenType.METHOD, "keyword");
            }

            if (tokenizer.tokenType() == TokenType.VOID) {
                process(TokenType.VOID, "keyword");
            } else {
                processType();
            }

            process(TokenType.IDENTIFIER, "identifier");
            process(TokenType.LEFT_PAREN, "symbol");
            writeStart("parameterList");
            if (tokenizer.tokenType() != TokenType.RIGHT_PAREN) {
                processType();
                process(TokenType.IDENTIFIER, "identifier");
                while (tokenizer.tokenType() == TokenType.COMMA) {
                    process(TokenType.COMMA, "symbol");
                    processType();
                    process(TokenType.IDENTIFIER, "identifier");
                }
            }
            writeEnd("parameterList");
            process(TokenType.RIGHT_PAREN, "symbol");

            writeStart("subroutineBody");
            process(TokenType.LEFT_BRACE, "symbol");
            while (tokenizer.tokenType() == TokenType.VAR) {
                writeStart("varDec");
                process(TokenType.VAR, "keyword");
                processType();
                process(TokenType.IDENTIFIER, "identifier");
                while (tokenizer.tokenType() == TokenType.COMMA) {
                    process(TokenType.COMMA, "symbol");
                    process(TokenType.IDENTIFIER, "identifier");
                }
                process(TokenType.SEMICOLON, "symbol");
                writeEnd("varDec");
            }
            compileStatements();
            process(TokenType.RIGHT_BRACE, "symbol");
            writeEnd("subroutineBody");
            writeEnd("subroutineDec");
        }

        process(TokenType.RIGHT_BRACE, "symbol");
        writeEnd("class");
    }

    private void compileStatements() throws IOException {
        writeStart("statements");
        while (true) {
            TokenType tokenType = tokenizer.tokenType();
            if (tokenType == TokenType.LET) {
                compileLet();
            } else if (tokenType == TokenType.IF) {
                compileIf();
            } else if (tokenType == TokenType.WHILE) {
                compileWhile();
            } else if (tokenType == TokenType.DO) {
                compileDo();
            } else if (tokenType == TokenType.RETURN) {
                compileReturn();
            } else {
                break;
            }
        }
        writeEnd("statements");
    }

    private void compileLet() throws IOException {
        writeStart("letStatement");
        process(TokenType.LET, "keyword");
        process(TokenType.IDENTIFIER, "identifier");
        if (tokenizer.tokenType() == TokenType.LEFT_BRACKET) {
            process(TokenType.LEFT_BRACKET, "symbol");
            compileExpression();
            process(TokenType.RIGHT_BRACKET, "symbol");
        }
        process(TokenType.EQUAL, "symbol");
        compileExpression();
        process(TokenType.SEMICOLON, "symbol");
        writeEnd("letStatement");
    }

    private void compileIf() throws IOException {
        writeStart("ifStatement");
        process(TokenType.IF, "keyword");
        process(TokenType.LEFT_PAREN, "symbol");
        compileExpression();
        process(TokenType.RIGHT_PAREN, "symbol");
        process(TokenType.LEFT_BRACE, "symbol");
        compileStatements();
        process(TokenType.RIGHT_BRACE, "symbol");
        if (tokenizer.tokenType() == TokenType.ELSE) {
            process(TokenType.ELSE, "keyword");
            process(TokenType.LEFT_BRACE, "symbol");
            compileStatements();
            process(TokenType.RIGHT_BRACE, "symbol");
        }
        writeEnd("ifStatement");
    }

    private void compileWhile() throws IOException {
        writeStart("whileStatement");
        process(TokenType.WHILE, "keyword");
        process(TokenType.LEFT_PAREN, "symbol");
        compileExpression();
        process(TokenType.RIGHT_PAREN, "symbol");
        process(TokenType.LEFT_BRACE, "symbol");
        compileStatements();
        process(TokenType.RIGHT_BRACE, "symbol");
        writeEnd("whileStatement");
    }

    private void compileDo() throws IOException {
        writeStart("doStatement");
        process(TokenType.DO, "keyword");
        process(TokenType.IDENTIFIER, "identifier");
        switch (tokenizer.tokenType()) {
            case LEFT_PAREN -> {
                process(TokenType.LEFT_PAREN, "symbol");
                compileExpressionList();
                process(TokenType.RIGHT_PAREN, "symbol");
            }
            case DOT -> {
                process(TokenType.DOT, "symbol");
                process(TokenType.IDENTIFIER, "identifier");
                process(TokenType.LEFT_PAREN, "symbol");
                compileExpressionList();
                process(TokenType.RIGHT_PAREN, "symbol");
            }
            default -> throw new IllegalStateException(
                "Unexpected token: " + tokenizer.tokenValue());
        }
        process(TokenType.SEMICOLON, "symbol");
        writeEnd("doStatement");
    }

    private void compileReturn() throws IOException {
        writeStart("returnStatement");
        process(TokenType.RETURN, "keyword");
        if (tokenizer.tokenType() != TokenType.SEMICOLON) {
            compileExpression();
        }
        process(TokenType.SEMICOLON, "symbol");
        writeEnd("returnStatement");
    }

    private void compileExpressionList() throws IOException {
        TokenType tokenType = tokenizer.tokenType();
        if (tokenType == TokenType.INTEGER
                || tokenType == TokenType.STRING
                || tokenType == TokenType.TRUE
                || tokenType == TokenType.FALSE
                || tokenType == TokenType.NULL
                || tokenType == TokenType.THIS
                || tokenType == TokenType.IDENTIFIER
                || tokenType == TokenType.LEFT_PAREN
                || tokenType == TokenType.MINUS
                || tokenType == TokenType.TILDE) {

            writeStart("expressionList");
            compileExpression();
            while (tokenizer.tokenType() == TokenType.COMMA) {
                process(TokenType.COMMA, "symbol");
                compileExpression();
            }
            writeEnd("expressionList");
        } else {
            writeStart("expressionList");
            writeEnd("expressionList");
        }
    }

    private void compileExpression() throws IOException {
        writeStart("expression");
        compileTerm();
        while (true) {
            TokenType tokenType = tokenizer.tokenType();
            if (tokenType == TokenType.PLUS) {
                process(TokenType.PLUS, "symbol");
            } else if (tokenType == TokenType.MINUS) {
                process(TokenType.MINUS, "symbol");
            } else if (tokenType == TokenType.STAR) {
                process(TokenType.STAR, "symbol");
            } else if (tokenType == TokenType.SLASH) {
                process(TokenType.SLASH, "symbol");
            } else if (tokenType == TokenType.AMPERSAND) {
                process(TokenType.AMPERSAND, "symbol");
            } else if (tokenType == TokenType.VBAR) {
                process(TokenType.VBAR, "symbol");
            } else if (tokenType == TokenType.LEFT_ANGLE) {
                process(TokenType.LEFT_ANGLE, "symbol");
            } else if (tokenType == TokenType.RIGHT_ANGLE) {
                process(TokenType.RIGHT_ANGLE, "symbol");
            } else if (tokenType == TokenType.EQUAL) {
                process(TokenType.EQUAL, "symbol");
            } else {
                break;
            }
            compileTerm();
        }
        writeEnd("expression");
    }

    private void compileTerm() throws IOException {
        writeStart("term");
        switch (tokenizer.tokenType()) {
            case INTEGER -> process(TokenType.INTEGER, "integerConstant");
            case STRING -> {
                write("stringConstant", tokenizer.string());
                tokenizer.advance();
            }
            case TRUE -> process(TokenType.TRUE, "keyword");
            case FALSE -> process(TokenType.FALSE, "keyword");
            case NULL -> process(TokenType.NULL, "keyword");
            case THIS -> process(TokenType.THIS, "keyword");
            case IDENTIFIER -> {
                process(TokenType.IDENTIFIER, "identifier");
                switch (tokenizer.tokenType()) {
                    case LEFT_BRACKET -> {
                        process(TokenType.LEFT_BRACKET, "symbol");
                        compileExpression();
                        process(TokenType.RIGHT_BRACKET, "symbol");
                    }
                    case LEFT_PAREN -> {
                        process(TokenType.LEFT_PAREN, "symbol");
                        compileExpressionList();
                        process(TokenType.RIGHT_PAREN, "symbol");
                    }
                    case DOT -> {
                        process(TokenType.DOT, "symbol");
                        process(TokenType.IDENTIFIER, "identifier");
                        process(TokenType.LEFT_PAREN, "symbol");
                        compileExpressionList();
                        process(TokenType.RIGHT_PAREN, "symbol");
                    }
                }
            }
            case LEFT_PAREN -> {
                process(TokenType.LEFT_PAREN, "symbol");
                compileExpression();
                process(TokenType.RIGHT_PAREN, "symbol");
            }
            case MINUS ->  {
                process(TokenType.MINUS, "symbol");
                compileTerm();
            }
            case TILDE -> {
                process(TokenType.TILDE, "symbol");
                compileTerm();
            }
            default -> throw new IllegalStateException(
                "Unexpected token: " + tokenizer.tokenValue());
        }
        writeEnd("term");
    }

    private void processType() throws IOException {
        switch (tokenizer.tokenType()) {
            case INT -> process(TokenType.INT, "keyword");
            case BOOLEAN -> process(TokenType.BOOLEAN, "keyword");
            case CHAR -> process(TokenType.CHAR, "keyword");
            case IDENTIFIER -> process(TokenType.IDENTIFIER, "identifier");
            default -> throw new IllegalStateException(
                "Unexpected token: " + tokenizer.tokenValue());
        }
    }

    private void process(TokenType tokenType, String element) throws IOException {
        if (tokenizer.tokenType() != tokenType) {
            throw new IllegalStateException(
                "Expected " + tokenType + ", got " + tokenizer.tokenType());
        }

        write(element, tokenizer.tokenValue());
        tokenizer.advance();
    }

    private void writeStart(String element) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.write(' ');
        }
        out.write("<");
        out.write(element);
        out.write(">\n");
        indent += 2;
    }

    private void writeEnd(String element) throws IOException {
        indent -= 2;
        for (int i = 0; i < indent; i++) {
            out.write(' ');
        }
        out.write("</");
        out.write(element);
        out.write(">\n");
    }

    private void write(String element, String text) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.write(' ');
        }
        out.write("<");
        out.write(element);
        out.write("> ");
        out.write(escapeXML(text));
        out.write(" </");
        out.write(element);
        out.write(">\n");
    }

    private String escapeXML(String str) {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
