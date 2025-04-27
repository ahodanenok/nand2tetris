package ahodanenok.nand2tetris.jack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ahodanenok.nand2tetris.jack.symbol.SymbolDef;
import ahodanenok.nand2tetris.jack.symbol.SymbolTable;
import ahodanenok.nand2tetris.jack.symbol.SymbolType;
import ahodanenok.nand2tetris.jack.vm.VmCodeWriter;
import ahodanenok.nand2tetris.jack.vm.VmSegment;

public class JackCompiler {

    private static final String JACK_FILE_EXT = ".jack";
    private static final String VM_FILE_EXT = ".vm";

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
                0, sourcePathName.length() - JACK_FILE_EXT.length()) + VM_FILE_EXT);
        try (BufferedWriter out = Files.newBufferedWriter(targetPath)) {
            JackCompiler compiler = new JackCompiler(code, out);
            compiler.compile();
        }
    }

    private final JackTokenizer tokenizer;
    private final Writer out;
    private final VmCodeWriter codeWriter;

    private String className;
    private SymbolTable classSymbolTable;
    private SymbolTable methodSymbolTable;
    private int labelIdx;

    public JackCompiler(String code, Writer out) {
        this.tokenizer = new JackTokenizer(code);
        this.codeWriter = new VmCodeWriter();
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
        classSymbolTable = new SymbolTable();

        match(TokenType.CLASS);
        expect(TokenType.IDENTIFIER);
        className = tokenizer.tokenValue();
        tokenizer.advance();
        match(TokenType.LEFT_BRACE);

        int staticIdx = 0;
        int fieldIdx = 0;
        while (tokenizer.tokenType() == TokenType.STATIC
                || tokenizer.tokenType() == TokenType.FIELD) {
            SymbolType symbolType;
            if (tokenizer.tokenType() == TokenType.STATIC) {
                symbolType = SymbolType.STATIC;
            } else {
                symbolType = SymbolType.FIELD;
            }
            tokenizer.advance();

            String valueType = processType();
            expect(TokenType.IDENTIFIER);
            if (symbolType == SymbolType.STATIC) {
                classSymbolTable.add(tokenizer.tokenValue(), valueType, symbolType, staticIdx++);
            } else {
                classSymbolTable.add(tokenizer.tokenValue(), valueType, symbolType, fieldIdx++);
            }
            tokenizer.advance();

            while (tokenizer.tokenType() == TokenType.COMMA) {
                match(TokenType.COMMA);
                expect(TokenType.IDENTIFIER);
                if (symbolType == SymbolType.STATIC) {
                    classSymbolTable.add(tokenizer.tokenValue(), valueType, symbolType, staticIdx++);
                } else {
                    classSymbolTable.add(tokenizer.tokenValue(), valueType, symbolType, fieldIdx++);
                }
                tokenizer.advance();
            }
            match(TokenType.SEMICOLON);
        }

        while (tokenizer.tokenType() == TokenType.CONSTRUCTOR
                || tokenizer.tokenType() == TokenType.FUNCTION
                || tokenizer.tokenType() == TokenType.METHOD) {
            methodSymbolTable = new SymbolTable();

            TokenType methodType = switch (tokenizer.tokenType()) {
                case CONSTRUCTOR -> TokenType.CONSTRUCTOR;
                case FUNCTION -> TokenType.FUNCTION;
                case METHOD -> TokenType.METHOD;
                default -> throw new IllegalStateException(
                    "Unexpected token: " + tokenizer.tokenType());
            };
            tokenizer.advance();

            if (tokenizer.tokenType() == TokenType.VOID) {
                match(TokenType.VOID);
            } else {
                processType();
            }

            expect(TokenType.IDENTIFIER);
            String methodName = tokenizer.tokenValue();
            tokenizer.advance();
            match(TokenType.LEFT_PAREN);
            int paramIdx = 0;
            if (tokenizer.tokenType() != TokenType.RIGHT_PAREN) {
                String valueType = processType();
                expect(TokenType.IDENTIFIER);
                if (methodType == TokenType.METHOD) {
                    methodSymbolTable.add(tokenizer.tokenValue(), valueType, SymbolType.ARGUMENT, paramIdx + 1);
                } else {
                    methodSymbolTable.add(tokenizer.tokenValue(), valueType, SymbolType.ARGUMENT, paramIdx);
                }
                paramIdx++;
                tokenizer.advance();

                while (tokenizer.tokenType() == TokenType.COMMA) {
                    match(TokenType.COMMA);
                    valueType = processType();
                    expect(TokenType.IDENTIFIER);
                    if (methodType == TokenType.METHOD) {
                        methodSymbolTable.add(tokenizer.tokenValue(), valueType, SymbolType.ARGUMENT, paramIdx + 1);
                    } else {
                        methodSymbolTable.add(tokenizer.tokenValue(), valueType, SymbolType.ARGUMENT, paramIdx);
                    }
                    paramIdx++;
                    tokenizer.advance();
                }
            }
            match(TokenType.RIGHT_PAREN);
            match(TokenType.LEFT_BRACE);

            int varIdx = 0;
            while (tokenizer.tokenType() == TokenType.VAR) {
                match(TokenType.VAR);
                String valueType = processType();
                expect(TokenType.IDENTIFIER);
                methodSymbolTable.add(tokenizer.tokenValue(), valueType, SymbolType.VARIABLE, varIdx++);
                tokenizer.advance();

                while (tokenizer.tokenType() == TokenType.COMMA) {
                    match(TokenType.COMMA);

                    expect(TokenType.IDENTIFIER);
                    methodSymbolTable.add(tokenizer.tokenValue(), valueType, SymbolType.VARIABLE, varIdx++);
                    tokenizer.advance();
                }
                match(TokenType.SEMICOLON);
            }
            codeWriter.writeFunction(
                String.format("%s.%s", className, methodName), varIdx, out);
            if (methodType == TokenType.CONSTRUCTOR) {
                codeWriter.writePush(VmSegment.CONSTANT, fieldIdx, out);
                codeWriter.writeCall(formatFunctionCall("Memory", "alloc"), 1, out);
                codeWriter.writePop(VmSegment.POINTER, 0, out);
            } else if (methodType == TokenType.METHOD) {
                codeWriter.writePush(VmSegment.ARGUMENT, 0, out);
                codeWriter.writePop(VmSegment.POINTER, 0, out);
            }
            compileStatements();
            match(TokenType.RIGHT_BRACE);
        }
        match(TokenType.RIGHT_BRACE);
    }

    private void compileStatements() throws IOException {
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
    }

    private void compileLet() throws IOException {
        match(TokenType.LET);
        expect(TokenType.IDENTIFIER);
        String symbolName = tokenizer.tokenValue();
        SymbolDef symbolDef = lookupSymbol(symbolName);
        tokenizer.advance();

        boolean isArray = false;
        if (tokenizer.tokenType() == TokenType.LEFT_BRACKET) {
            isArray = true;
            match(TokenType.LEFT_BRACKET);
            compileExpression();
            codeWriter.writePush(symbolSegment(symbolDef), symbolDef.index, out);
            codeWriter.writeAdd(out);
            codeWriter.writePop(VmSegment.TEMP, 1, out);
            match(TokenType.RIGHT_BRACKET);
        }

        match(TokenType.EQUAL);
        compileExpression();
        match(TokenType.SEMICOLON);
        if (isArray) {
            codeWriter.writePush(VmSegment.TEMP, 1, out);
            codeWriter.writePop(VmSegment.POINTER, 1, out);
            codeWriter.writePop(VmSegment.THAT, 0, out);
        } else {
            codeWriter.writePop(symbolSegment(symbolDef), symbolDef.index, out);
        }
    }

    private void compileIf() throws IOException {
        String elseLabel = "$IF_ELSE_" + labelIdx++;
        String endLabel = "$IF_END_" + labelIdx++;

        match(TokenType.IF);
        match(TokenType.LEFT_PAREN);
        compileExpression();
        codeWriter.writeNot(out);
        codeWriter.writeIfGoto(elseLabel, out);
        match(TokenType.RIGHT_PAREN);
        match(TokenType.LEFT_BRACE);
        compileStatements();
        match(TokenType.RIGHT_BRACE);
        if (tokenizer.tokenType() == TokenType.ELSE) {
            codeWriter.writeGoto(endLabel, out);
            codeWriter.writeLabel(elseLabel, out);
            match(TokenType.ELSE);
            match(TokenType.LEFT_BRACE);
            compileStatements();
            match(TokenType.RIGHT_BRACE);
            codeWriter.writeLabel(endLabel, out);
        } else {
            codeWriter.writeLabel(elseLabel, out);
            codeWriter.writeLabel(endLabel, out);
        }
    }

    private void compileWhile() throws IOException {
        String startLabel = "$WHILE_LOOP_" + labelIdx++;
        String endLabel = "$WHILE_END_" + labelIdx++;

        match(TokenType.WHILE);
        match(TokenType.LEFT_PAREN);
        codeWriter.writeLabel(startLabel, out);
        compileExpression();
        codeWriter.writeNot(out);
        match(TokenType.RIGHT_PAREN);
        codeWriter.writeIfGoto(endLabel, out);
        match(TokenType.LEFT_BRACE);
        compileStatements();
        match(TokenType.RIGHT_BRACE);
        codeWriter.writeGoto(startLabel, out);
        codeWriter.writeLabel(endLabel, out);
    }

    private void compileDo() throws IOException {
        match(TokenType.DO);
        expect(TokenType.IDENTIFIER);
        String name1 = tokenizer.tokenValue();
        tokenizer.advance();

        switch (tokenizer.tokenType()) {
            case LEFT_PAREN -> {
                match(TokenType.LEFT_PAREN);
                codeWriter.writePush(VmSegment.POINTER, 0, out);
                int argumentCount = compileExpressionList();
                match(TokenType.RIGHT_PAREN);

                codeWriter.writeCall(formatFunctionCall(className, name1), argumentCount + 1, out);
                codeWriter.writePop(VmSegment.TEMP, 0, out);
            }
            case DOT -> {
                match(TokenType.DOT);

                expect(TokenType.IDENTIFIER);
                String name2 = tokenizer.tokenValue();
                tokenizer.advance();

                boolean isMethod = classSymbolTable.contains(name1)
                    || methodSymbolTable.contains(name1);

                match(TokenType.LEFT_PAREN);
                int argumentCount = 0;
                if (isMethod) {
                    SymbolDef symbolDef = lookupSymbol(name1);
                    codeWriter.writePush(symbolSegment(symbolDef), symbolDef.index, out);
                    argumentCount++;
                }
                argumentCount += compileExpressionList();
                match(TokenType.RIGHT_PAREN);

                if (isMethod) {
                    SymbolDef symbolDef = lookupSymbol(name1);
                    codeWriter.writeCall(formatFunctionCall(symbolDef.valueType, name2), argumentCount, out);
                } else {
                    codeWriter.writeCall(formatFunctionCall(name1, name2), argumentCount, out);
                }
                codeWriter.writePop(VmSegment.TEMP, 0, out);
            }
            default -> throw new IllegalStateException(
                "Unexpected token: " + tokenizer.tokenValue());
        }
        match(TokenType.SEMICOLON);
    }

    private void compileReturn() throws IOException {
        match(TokenType.RETURN);
        if (tokenizer.tokenType() != TokenType.SEMICOLON) {
            compileExpression();
        } else {
            codeWriter.writePush(VmSegment.CONSTANT, 0, out);
        }
        match(TokenType.SEMICOLON);
        codeWriter.writeReturn(out);
    }

    private int compileExpressionList() throws IOException {
        int count = 0;
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

            compileExpression();
            count++;
            while (tokenizer.tokenType() == TokenType.COMMA) {
                match(TokenType.COMMA);
                compileExpression();
                count++;
            }
        }

        return count;
    }

    private void compileExpression() throws IOException {
        compileTerm();
        while (true) {
            TokenType tokenType = tokenizer.tokenType();
            if (tokenType == TokenType.PLUS) {
                match(TokenType.PLUS);
                compileTerm();
                codeWriter.writeAdd(out);
            } else if (tokenType == TokenType.MINUS) {
                match(TokenType.MINUS);
                compileTerm();
                codeWriter.writeSub(out);
            } else if (tokenType == TokenType.STAR) {
                match(TokenType.STAR);
                compileTerm();
                codeWriter.writeCall(formatFunctionCall("Math", "multiply"), 2, out);
            } else if (tokenType == TokenType.SLASH) {
                match(TokenType.SLASH);
                compileTerm();
                codeWriter.writeCall(formatFunctionCall("Math", "divide"), 2, out);
            } else if (tokenType == TokenType.AMPERSAND) {
                match(TokenType.AMPERSAND);
                compileTerm();
                codeWriter.writeAnd(out);
            } else if (tokenType == TokenType.VBAR) {
                match(TokenType.VBAR);
                compileTerm();
                codeWriter.writeOr(out);
            } else if (tokenType == TokenType.LEFT_ANGLE) {
                match(TokenType.LEFT_ANGLE);
                compileTerm();
                codeWriter.writeLt(out);
            } else if (tokenType == TokenType.RIGHT_ANGLE) {
                match(TokenType.RIGHT_ANGLE);
                compileTerm();
                codeWriter.writeGt(out);
            } else if (tokenType == TokenType.EQUAL) {
                match(TokenType.EQUAL);
                compileTerm();
                codeWriter.writeEq(out);
            } else {
                break;
            }
        }
    }

    private void compileTerm() throws IOException {
        switch (tokenizer.tokenType()) {
            case INTEGER -> {
                codeWriter.writePush(VmSegment.CONSTANT, tokenizer.integer(), out);
                tokenizer.advance();
            }
            case STRING -> {
                String str = tokenizer.string();
                codeWriter.writePush(VmSegment.CONSTANT, str.length(), out);
                codeWriter.writeCall(formatFunctionCall("String", "new"), 1, out);
                for (int i = 0; i < str.length(); i++) {
                    codeWriter.writePush(VmSegment.CONSTANT, (int) str.charAt(i), out);
                    codeWriter.writeCall(formatFunctionCall("String", "appendChar"), 2, out);
                }
                tokenizer.advance();
            }
            case TRUE -> {
                match(TokenType.TRUE);
                codeWriter.writePush(VmSegment.CONSTANT, 1, out);
                codeWriter.writeNeg(out);
            }
            case FALSE -> {
                match(TokenType.FALSE);
                codeWriter.writePush(VmSegment.CONSTANT, 0, out);
            }
            case NULL -> {
                match(TokenType.NULL);
                codeWriter.writePush(VmSegment.CONSTANT, 0, out);
            }
            case THIS -> {
                match(TokenType.THIS);
                codeWriter.writePush(VmSegment.POINTER, 0, out);
            }
            case IDENTIFIER -> {
                expect(TokenType.IDENTIFIER);
                String name1 = tokenizer.tokenValue();
                tokenizer.advance();

                switch (tokenizer.tokenType()) {
                    case LEFT_BRACKET -> {
                        SymbolDef symbolDef = lookupSymbol(name1);
                        match(TokenType.LEFT_BRACKET);
                        compileExpression();
                        codeWriter.writePush(symbolSegment(symbolDef), symbolDef.index, out);
                        codeWriter.writeAdd(out);
                        codeWriter.writePop(VmSegment.POINTER, 1, out);
                        codeWriter.writePush(VmSegment.THAT, 0, out);
                        match(TokenType.RIGHT_BRACKET);
                    }
                    case LEFT_PAREN -> {
                        match(TokenType.LEFT_PAREN);
                        codeWriter.writePush(VmSegment.POINTER, 0, out);
                        int argumentCount = compileExpressionList();
                        match(TokenType.RIGHT_PAREN);
                        codeWriter.writeCall(
                            formatFunctionCall(className, name1), argumentCount + 1, out);
                    }
                    case DOT -> {
                        match(TokenType.DOT);
                        expect(TokenType.IDENTIFIER);
                        String name2 = tokenizer.tokenValue();
                        tokenizer.advance();

                        boolean isMethod = classSymbolTable.contains(name1)
                            || methodSymbolTable.contains(name1);

                        match(TokenType.LEFT_PAREN);
                        int argumentCount = 0;
                        if (isMethod) {
                            SymbolDef symbolDef = lookupSymbol(name1);
                            codeWriter.writePush(symbolSegment(symbolDef), symbolDef.index, out);
                            argumentCount++;
                        }
                        argumentCount += compileExpressionList();
                        match(TokenType.RIGHT_PAREN);

                        if (isMethod) {
                            SymbolDef symbolDef = lookupSymbol(name1);
                            codeWriter.writeCall(formatFunctionCall(symbolDef.valueType, name2), argumentCount, out);
                        } else {
                            codeWriter.writeCall(formatFunctionCall(name1, name2), argumentCount, out);
                        }
                    }
                    default -> {
                        SymbolDef symbolDef = lookupSymbol(name1);
                        codeWriter.writePush(symbolSegment(symbolDef), symbolDef.index, out);
                    }
                }
            }
            case LEFT_PAREN -> {
                match(TokenType.LEFT_PAREN);
                compileExpression();
                match(TokenType.RIGHT_PAREN);
            }
            case MINUS ->  {
                match(TokenType.MINUS);
                compileTerm();
                codeWriter.writeNeg(out);
            }
            case TILDE -> {
                match(TokenType.TILDE);
                compileTerm();
                codeWriter.writeNot(out);
            }
            default -> throw new IllegalStateException(
                "Unexpected token: " + tokenizer.tokenValue());
        }
    }

    private String processType() throws IOException {
        return switch (tokenizer.tokenType()) {
            case INT -> {
                match(TokenType.INT);
                yield "int";
            }
            case BOOLEAN -> {
                match(TokenType.BOOLEAN);
                yield "boolean";
            }
            case CHAR -> {
                match(TokenType.CHAR);
                yield "char";
            }
            case IDENTIFIER -> {
                String type = tokenizer.tokenValue();
                match(TokenType.IDENTIFIER);
                yield type;
            }
            default -> throw new IllegalStateException(
                "Unexpected token: " + tokenizer.tokenValue());
        };
    }

    private SymbolDef lookupSymbol(String symbolName) {
        SymbolDef symbolDef = classSymbolTable.get(symbolName);
        if (symbolDef != null) {
            return symbolDef;
        }

        symbolDef = methodSymbolTable.get(symbolName);
        if (symbolDef == null) {
            throw new IllegalStateException("Unknown symbol: " + symbolName);
        }

        return symbolDef;
    }

    private VmSegment symbolSegment(SymbolDef symbolDef) {
        return switch (symbolDef.symbolType) {
            case FIELD -> VmSegment.THIS;
            case STATIC -> VmSegment.STATIC;
            case ARGUMENT -> VmSegment.ARGUMENT;
            case VARIABLE -> VmSegment.LOCAL;
        };
    }

    private String formatFunctionCall(String className, String functionName) {
        return String.format("%s.%s", className, functionName);
    }

    private void expect(TokenType tokenType) {
        if (tokenizer.tokenType() != tokenType) {
            throw new IllegalStateException(
                "Expected " + tokenType + ", got " + tokenizer.tokenType());
        }
    }

    private void match(TokenType tokenType) {
        expect(tokenType);
        tokenizer.advance();
    }
}
