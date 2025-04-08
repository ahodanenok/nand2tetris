package ahodanenok.nand2tetris.vm.translator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

public class VmTranslator {

    private static final String VM_FILE_EXT = ".vm";
    private static final String ASM_FILE_EXT = ".asm";

    public static void main(String... args) throws Exception {
        Path sourcePath = Paths.get(args[0]);
        if (Files.isDirectory(sourcePath)) {
            translateDir(sourcePath);
        } else if (Files.isRegularFile(sourcePath)) {
            translateFile(sourcePath);
        } else {
            System.out.println("Not a regular file or a directory");
            System.exit(-1);
        }
    }

    private static void translateFile(Path vmFile) throws IOException {
        String vmFileName = vmFile.getFileName().toString();
        if (!vmFileName.endsWith(VM_FILE_EXT)) {
            System.out.println("Not a vm code file");
            System.exit(-1);
            return;
        }

        String asmFileName = vmFileName.substring(
            0, vmFileName.length() - VM_FILE_EXT.length()) + ASM_FILE_EXT;
        Path asmFile = vmFile.getParent().resolve(asmFileName);
        System.out.printf("Translating: %s -> %s...", vmFile, asmFile);

        VmTranslator translator = new VmTranslator();
        String code = new String(Files.readAllBytes(vmFile), "UTF-8");
        try (BufferedWriter out = Files.newBufferedWriter(asmFile)) {
            translator.init(out);
            translator.translate(code, out);
            System.out.println("done!");
        }
    }

    private static void translateDir(Path dir) throws IOException {
        Iterator<Path> vmFiles = Files.newDirectoryStream(
            dir, p -> p.getFileName().toString().endsWith(VM_FILE_EXT)).iterator();
        if (!vmFiles.hasNext()) {
            System.out.println("No vm files in the directory");
            System.exit(-1);
            return;
        }

        Path asmFile = dir.resolve(dir.getFileName().toString() + ASM_FILE_EXT);
        System.out.printf("Translating files in '%s' to '%s'%n", dir, asmFile);
        VmTranslator translator = new VmTranslator();
        try (BufferedWriter out = Files.newBufferedWriter(asmFile)) {
            translator.init(out);
            while (vmFiles.hasNext()) {
                Path vmFile = vmFiles.next();
                String code = new String(Files.readAllBytes(vmFile), "UTF-8");
                System.out.printf("  %s...", vmFile.getFileName());
                translator.translate(code, out);
                System.out.println("done!");
            }
        }
        System.out.println("All done!");
    }

    private final VmHackGenerator hack = new VmHackGenerator();

    public void init(Writer out) throws IOException {
        hack.init(out);
    }

    public void translate(String code, Writer out) throws IOException {
        VmParser parser = new VmParser(code);
        while (parser.advance()) {
            switch (parser.command()) {
                case PUSH -> hack.push(parser.segment(), parser.number(), out);
                case POP -> hack.pop(parser.segment(), parser.number(), out);
                case ADD -> hack.add(out);
                case SUB -> hack.sub(out);
                case NEG -> hack.neg(out);
                case EQ -> hack.eq(out);
                case LT -> hack.lt(out);
                case GT -> hack.gt(out);
                case AND -> hack.and(out);
                case OR -> hack.or(out);
                case NOT -> hack.not(out);
                case LABEL -> hack.label(parser.label(), out);
                case GOTO -> hack.uGoto(parser.label(), out);
                case IF_GOTO -> hack.ifGoto(parser.label(), out);
                case FUNCTION -> hack.function(parser.label(), parser.number(), out);
                case CALL -> hack.call(parser.label(), parser.number(), out);
                case RETURN -> hack.ret(out);
            }
        }
    }
}
