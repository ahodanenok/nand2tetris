package ahodanenok.nand2tetris.vm.translator;

import java.io.IOException;
import java.io.Writer;

public class VmHackGenerator {

    private int nextLabelIdx = 0;

    public void push(VmSegment segment, int index, Writer out) throws IOException {
        switch (segment) {
            case ARGUMENT -> {
                segmentReadD("R2", index, out);
                pushD(out);
            }
            case CONSTANT -> {
                out.write(at(index + ""));
                out.write("D=A\n");
                pushD(out);
            }
            case LOCAL -> {
                segmentReadD("LCL", index, out);
                pushD(out);
            }
            case POINTER -> {
                if (index == 0) {
                    out.write(at("THIS"));
                    out.write("D=M\n");
                    pushD(out);
                } else if (index == 1) {
                    out.write(at("THAT"));
                    out.write("D=M\n");
                    pushD(out);
                } else {
                    throw new IllegalStateException("Illegal index for pointer segment: " + index);
                }
            }
            case STATIC -> {
                segmentReadD("16", index, out);
                pushD(out);
            }
            case TEMP -> {
                out.write(at(index + ""));
                out.write("D=A\n");
                out.write(at("R5"));
                out.write("A=D+A\n");
                out.write("D=M\n");
                pushD(out);
            }
            case THAT -> {
                segmentReadD("THAT", index, out);
                pushD(out);
            }
            case THIS -> {
                segmentReadD("THIS", index, out);
                pushD(out);
            }
        }
    }

    private void segmentReadD(String baseLabel, int index, Writer out) throws IOException {
        out.write(at(index + ""));
        out.write("D=A\n");
        out.write(at(baseLabel));
        out.write("A=M\n");
        out.write("A=A+D\n");
        out.write("D=M\n");
    }

    private void pushD(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("A=M\n");
        out.write("M=D\n");
        out.write(at("SP"));
        out.write("M=M+1\n");
    }

    public void pop(VmSegment segment, int index, Writer out) throws IOException {
        switch (segment) {
            case ARGUMENT -> {
                segmentAddress("R2", index, "R13", out);
                popD(out);
                segmentWriteD("R13", out);
            }
            case CONSTANT ->
                throw new IllegalStateException("Constant segment is non writable");
            case LOCAL -> {
                segmentAddress("LCL", index, "R13", out);
                popD(out);
                segmentWriteD("R13", out);
            }
            case POINTER -> {
                if (index == 0) {
                    popD(out);
                    out.write(at("THIS"));
                    out.write("M=D\n");
                } else if (index == 1) {
                    popD(out);
                    out.write(at("THAT"));
                    out.write("M=D\n");
                } else {
                    throw new IllegalStateException("Illegal index for pointer segment: " + index);
                }
            }
             case STATIC -> {
                segmentAddress("16", index, "R13", out);
                popD(out);
                segmentWriteD("R13", out);
            }
            case TEMP -> {
                out.write(at(index + ""));
                out.write("D=A\n");
                out.write(at("R5"));
                out.write("D=D+A\n");
                out.write(at("R13"));
                out.write("M=D\n");
                popD(out);
                segmentWriteD("R13", out);
            }
            case THAT -> {
                segmentAddress("THAT", index, "R13", out);
                popD(out);
                segmentWriteD("R13", out);
            }
            case THIS -> {
                segmentAddress("THIS", index, "R13", out);
                popD(out);
                segmentWriteD("R13", out);
            }
        }
    }

    private void segmentAddress(String baseLabel, int index, String segmentAddressLabel, Writer out) throws IOException {
        out.write(at(index + ""));
        out.write("D=A\n");
        out.write(at(baseLabel));
        out.write("A=M\n");
        out.write("D=A+D\n");
        out.write(at(segmentAddressLabel));
        out.write("M=D\n");
    }

    private void segmentWriteD(String segmentAddressLabel, Writer out) throws IOException {
        out.write(at(segmentAddressLabel));
        out.write("A=M\n");
        out.write("M=D\n");
    }

    private void popD(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("M=M-1\n");
    }

    public void add(Writer out) throws IOException {
        out.write("@SP\n");
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write("@SP\n");
        out.write("A=M-1\n");
        out.write("M=D+M\n");
    }

    public void sub(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=M-D\n");
    }

    public void neg(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=-M\n");
    }

    public void eq(Writer out) throws IOException {
        String yes = newLabelName();
        String no = newLabelName();
        String end = newLabelName();

        out.write(at("SP"));
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("D=M-D\n");
        out.write(at(yes));
        out.write("D;JEQ\n");
        out.write(at(no));
        out.write("0;JMP\n");
        out.write(label(yes));
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=-1\n");
        out.write(at(end));
        out.write("0;JMP\n");
        out.write(label(no));
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=0\n");
        out.write(label(end));
    }

    public void gt(Writer out) throws IOException {
        String yes = newLabelName();
        String no = newLabelName();
        String end = newLabelName();

        out.write(at("SP"));
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("D=M-D\n");
        out.write(at(yes));
        out.write("D;JGT\n");
        out.write(at(no));
        out.write("0;JMP\n");
        out.write(label(yes));
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=-1\n");
        out.write(at(end));
        out.write("0;JMP\n");
        out.write(label(no));
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=0\n");
        out.write(label(end));
    }

    public void lt(Writer out) throws IOException {
        String yes = newLabelName();
        String no = newLabelName();
        String end = newLabelName();

        out.write(at("SP"));
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("D=M-D\n");
        out.write(at(yes));
        out.write("D;JLT\n");
        out.write(at(no));
        out.write("0;JMP\n");
        out.write(label(yes));
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=-1\n");
        out.write(at(end));
        out.write("0;JMP\n");
        out.write(label(no));
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=0\n");
        out.write(label(end));
    }

    public void and(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=D&M\n");
    }

    public void or(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("M=M-1\n");
        out.write("A=M\n");
        out.write("D=M\n");
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=D|M\n");
    }

    public void not(Writer out) throws IOException {
        out.write(at("SP"));
        out.write("A=M-1\n");
        out.write("M=!M\n");
    }

    private String newLabelName() {
        String label = "LB_" + nextLabelIdx;
        nextLabelIdx++;
        return label;
    }

    private String at(String address) {
        return "@" + address + "\n";
    }

    private String label(String name) {
        return "(" + name + ")\n";
    }
}
