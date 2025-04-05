package ahodanenok.nand2tetris.vm.translator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VmTranslator {

    private static final String VM_FILE_EXT = ".vm";
    private static final String ASM_FILE_EXT = ".asm";

    public static void main(String... args) throws Exception {
        Path sourceFile = Paths.get(args[0]);
        if (!Files.isRegularFile(sourceFile)) {
            System.out.println("Not a regular file");
            System.exit(-1);
            return;
        }

        String sourceFileName = sourceFile.getFileName().toString();
        if (!sourceFileName.endsWith(VM_FILE_EXT)) {
            System.out.println("Not a vm code file");
            System.exit(-1);
            return;
        }

        String targetFileName = sourceFileName.substring(
            0, sourceFileName.length() - VM_FILE_EXT.length()) + ASM_FILE_EXT;
        Path targetFile = sourceFile.getParent().resolve(targetFileName);
        System.out.printf("Translating: %s -> %s...", sourceFile, targetFile);

        VmTranslator translator = new VmTranslator();
        String code = new String(Files.readAllBytes(sourceFile), "UTF-8");
        try (BufferedWriter out = Files.newBufferedWriter(targetFile)) {
            translator.translate(code, out);
            System.out.println("done!");
        }
    }

    private final VmHackGenerator hack = new VmHackGenerator();

    public void translate(String code, Writer out) throws IOException {
        VmParser parser = new VmParser(code);
        while (parser.advance()) {
            switch (parser.command()) {
                case PUSH -> hack.push(parser.segment(), parser.index(), out);
                case POP -> hack.pop(parser.segment(), parser.index(), out);
                case ADD -> hack.add(out);
                case SUB -> hack.sub(out);
                case NEG -> hack.neg(out);
                case EQ -> hack.eq(out);
                case LT -> hack.lt(out);
                case GT -> hack.gt(out);
                case AND -> hack.and(out);
                case OR -> hack.or(out);
                case NOT -> hack.not(out);
            }
        }
    }
}
