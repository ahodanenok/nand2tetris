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
                assembler.translate(code, out);
                System.out.println("done!");
            }
        }
    }

    private final Hack hack;

    public Assembler() {
        this.hack = new Hack();
    }

    public void translate(String asm, Writer out) throws IOException {
        Parser parser = new Parser(asm);
        boolean first = true;
        while (parser.advance()) {
            if (!first) {
                out.write("\n");
            }

            InstructionType instType = parser.instType();
            if (instType == InstructionType.A) {
                translateA(parser, out);
            } else if (instType == InstructionType.C) {
                translateC(parser, out);
            } else if (instType == InstructionType.L) {
                //parser.symbol();
            } else {
                throw new IllegalStateException("Unsupported instruction type: " + instType);
            }

            first = false;
        }
    }

    private void translateA(Parser parser, Writer out) throws IOException {
        int value;
        if (parser.symbol() != null) {
            value = 0; // todo: use symbol table
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
