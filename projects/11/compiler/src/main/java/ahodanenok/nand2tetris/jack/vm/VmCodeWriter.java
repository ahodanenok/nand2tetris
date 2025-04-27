package ahodanenok.nand2tetris.jack.vm;

import java.io.IOException;
import java.io.Writer;

public class VmCodeWriter {

    public void writePush(VmSegment segment, int index, Writer out) throws IOException {
        out.write("push ");
        out.write(segment.name().toLowerCase());
        out.write(" ");
        out.write(String.valueOf(index));
        out.write("\n");
    }

    public void writePop(VmSegment segment, int index, Writer out) throws IOException {
        out.write("pop ");
        out.write(segment.name().toLowerCase());
        out.write(" ");
        out.write(String.valueOf(index));
        out.write("\n");
    }

    public void writeAdd(Writer out) throws IOException {
        out.write("add\n");
    }

    public void writeSub(Writer out) throws IOException {
        out.write("sub\n");
    }

    public void writeNeg(Writer out) throws IOException {
        out.write("neg\n");
    }

    public void writeEq(Writer out) throws IOException {
        out.write("eq\n");
    }

    public void writeGt(Writer out) throws IOException {
        out.write("gt\n");
    }

    public void writeLt(Writer out) throws IOException {
        out.write("lt\n");
    }

    public void writeAnd(Writer out) throws IOException {
        out.write("and\n");
    }

    public void writeOr(Writer out) throws IOException {
        out.write("or\n");
    }

    public void writeNot(Writer out) throws IOException {
        out.write("not\n");
    }

    public void writeLabel(String labelName, Writer out) throws IOException {
        out.write("label ");
        out.write(labelName);
        out.write("\n");
    }

    public void writeGoto(String labelName, Writer out) throws IOException {
        out.write("goto ");
        out.write(labelName);
        out.write("\n");
    }

    public void writeIfGoto(String labelName, Writer out) throws IOException {
        out.write("if-goto ");
        out.write(labelName);
        out.write("\n");
    }

    public void writeFunction(String functionName, int parameterCount, Writer out) throws IOException {
        out.write("function ");
        out.write(functionName);
        out.write(" ");
        out.write(String.valueOf(parameterCount));
        out.write("\n");
    }

    public void writeCall(String functionName, int argumentCount, Writer out) throws IOException {
        out.write("call ");
        out.write(functionName);
        out.write(" ");
        out.write(String.valueOf(argumentCount));
        out.write("\n");
    }

    public void writeReturn(Writer out) throws IOException {
        out.write("return\n");
    }
}
