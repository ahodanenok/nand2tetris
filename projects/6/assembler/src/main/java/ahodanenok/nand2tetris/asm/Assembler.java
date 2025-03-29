package ahodanenok.nand2tetris.asm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public final class Assembler {

    private static final String ASM_FILE_EXT = ".asm";
    private static final String HACK_FILE_EXT = ".hack";

    public static void main(String... args) throws IOException {
        Path path = Paths.get(args[0]);

        Iterator<Path> files;
        if (Files.isDirectory(path)) {
            files = Files.newDirectoryStream(
                path,
                p -> Files.isRegularFile(p) && p.getFileName().endsWith(ASM_FILE_EXT)).iterator();
        } else if (Files.isRegularFile(path)) {
            if (!path.getFileName().toString().endsWith(ASM_FILE_EXT)) {
                System.out.println("Given file is not an assembly file");
                System.exit(-1);
            }

            files = List.of(path).iterator();
        } else {
            System.out.println("Given path must be a directory or a file");
            System.exit(-1);
            return;
        }

        Assembler assembler = new Assembler();
        while (files.hasNext()) {
            Path asmFile = files.next();
            String asmFileName = asmFile.getFileName().toString();
            Path hackFile = asmFile.getParent().resolve(
                asmFileName.substring(0, asmFileName.length() - ASM_FILE_EXT.length()) + HACK_FILE_EXT);

            String code = new String(Files.readAllBytes(asmFile), "UTF-8");
            try (BufferedWriter out = Files.newBufferedWriter(hackFile)) {
                System.out.printf("%s -> %s...", asmFile, hackFile);
                assembler.resolve(code);
                assembler.translate(code, out);
                System.out.println("done!");
            }
        }
    }

    private final Hack hack;
    private final SymbolTable symbols;
    private int nextVarAddress = 16;
    private int asmLine = 0;

    public Assembler() {
        this.hack = new Hack();
        this.symbols = new SymbolTable();

        symbols.assign("SP", 0);
        symbols.assign("LCL", 1);
        symbols.assign("ARG", 2);
        symbols.assign("THIS", 3);
        symbols.assign("THAT", 4);
        symbols.assign("SCREEN", 16384);
        symbols.assign("KBD", 24576);
        symbols.assign("R0", 0);
        symbols.assign("R1", 1);
        symbols.assign("R2", 2);
        symbols.assign("R3", 3);
        symbols.assign("R4", 4);
        symbols.assign("R5", 5);
        symbols.assign("R6", 6);
        symbols.assign("R7", 7);
        symbols.assign("R8", 8);
        symbols.assign("R9", 9);
        symbols.assign("R10", 10);
        symbols.assign("R11", 11);
        symbols.assign("R12", 12);
        symbols.assign("R13", 13);
        symbols.assign("R14", 14);
        symbols.assign("R15", 15);
    }

    public void resolve(String asm) {
        Parser parser = new Parser(asm);
        while (parser.advance()) {
            InstructionType instType = parser.instType();
            if (instType == InstructionType.A) {
                asmLine++;
            } else if (instType == InstructionType.C) {
                asmLine++;
            } else if (instType == InstructionType.L) {
                symbols.assign(parser.symbol(), asmLine);
            } else {
                throw new IllegalStateException("Unsupported instruction type: " + instType);
            }
        }
    }

    public void translate(String asm, Writer out) throws IOException {
        Parser parser = new Parser(asm);
        boolean first = true;
        while (parser.advance()) {
            InstructionType instType = parser.instType();
            if (instType == InstructionType.A) {
                if (!first) {
                    out.write("\n");
                }
                translateA(parser, out);
                first = false;
            } else if (instType == InstructionType.C) {
                if (!first) {
                    out.write("\n");
                }
                translateC(parser, out);
                first = false;
            } else if (instType == InstructionType.L) {
                // skip
            } else {
                throw new IllegalStateException("Unsupported instruction type: " + instType);
            }
        }
    }

    private void translateA(Parser parser, Writer out) throws IOException {
        int value;
        if (parser.symbol() != null) {
            if (symbols.exists(parser.symbol())) {
                value = symbols.lookup(parser.symbol());
            } else {
                value = nextVarAddress;
                symbols.assign(parser.symbol(), nextVarAddress);
                nextVarAddress++;
            }
        } else if (parser.number() != -1) {
            value = parser.number();
        } else {
            throw new IllegalStateException("A-instruction must contain either a decimal value or a variable");
        }

        String inst = Integer.toString(value, 2);
        while (inst.length() < 16) {
            inst = "0" + inst;
        }

        out.write(inst);
    }

    private void translateC(Parser parser, Writer out) throws IOException {
        out.write("111");
        out.write(hack.encodeComp(parser.comp()));
        out.write(hack.encodeDest(parser.dest()));
        out.write(hack.encodeJump(parser.jump()));
    }
}
