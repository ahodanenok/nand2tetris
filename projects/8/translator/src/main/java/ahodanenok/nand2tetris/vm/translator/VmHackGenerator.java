package ahodanenok.nand2tetris.vm.translator;

import java.io.IOException;
import java.io.Writer;

public class VmHackGenerator {

    private String file = "Main";
    private String function = "main";
    private int nextLabelIdx = 0;

    public void init(Writer out) throws IOException {
        out.write(at("256"));
        out.write("D=A\n");
        out.write(at("SP"));
        out.write("M=D\n");
        call("Sys.init", 0, out);

        String loopLabel = labelName("LOOP");
        out.write(label(loopLabel));
        out.write(at(loopLabel));
        out.write("0;JMP\n");
    }

    public void label(String name, Writer out) throws IOException {
        out.write(label(labelName(name)));
    }

    public void uGoto(String label, Writer out) throws IOException {
        out.write(at(labelName(label)));
        out.write("0;JMP\n");
    }

    public void ifGoto(String label, Writer out) throws IOException {
        popD(out);
        out.write(at(labelName(label)));
        out.write("D;JNE\n");
    }

    public void function(String name, int variableCount, Writer out) throws IOException {
        String[] parts = name.split("\\.");
        file = parts[0];
        function = parts[1];

        out.write(label(name));
        out.write("D=0\n");
        for (int i = 0; i < variableCount; i++) {
            pushD(out);
        }
    }

    public void call(String name, int argumentCount, Writer out) throws IOException {
        String returnLabel = labelName();

        // push return address
        out.write(at(returnLabel));
        out.write("D=A\n");
        pushD(out);
        // push LCL
        out.write(at("LCL"));
        out.write("D=M\n");
        pushD(out);
        // push ARG
        out.write(at("ARG"));
        out.write("D=M\n");
        pushD(out);
        // push THIS
        out.write(at("THIS"));
        out.write("D=M\n");
        pushD(out);
        // push THAT
        out.write(at("THAT"));
        out.write("D=M\n");
        pushD(out);

        // ARG = SP-5-argumentCount
        out.write(at("5"));
        out.write("D=A\n");
        out.write(at("SP"));
        out.write("D=M-D\n");
        out.write(at(argumentCount + ""));
        out.write("D=D-A\n");
        out.write(at("ARG"));
        out.write("M=D\n");

        // LCL = SP
        out.write(at("SP"));
        out.write("D=M\n");
        out.write(at("LCL"));
        out.write("M=D\n");

        // jump to the function
        out.write(at(name));
        out.write("0;JMP\n");
        out.write(label(returnLabel));
    }

    public void ret(Writer out) throws IOException {
        // save return address
        out.write(at("5"));
        out.write("D=A\n");
        out.write(at("LCL"));
        out.write("A=M-D\n");
        out.write("D=M\n");
        out.write(at("R14"));
        out.write("M=D\n");
        // reposition return value
        popD(out);
        out.write(at("ARG"));
        out.write("A=M\n");
        out.write("M=D\n");
        // reposition stack
        out.write(at("ARG"));
        out.write("D=M+1\n");
        out.write(at("SP"));
        out.write("M=D\n");

        out.write(at("LCL"));
        out.write("D=M\n");
        out.write(at("R15"));
        out.write("M=D\n");
        // restore THAT
        out.write("A=D-1\n");
        out.write("D=M\n");
        out.write(at("THAT"));
        out.write("M=D\n");
        // restore THIS
        out.write(at("2"));
        out.write("D=A\n");
        out.write(at("R15"));
        out.write("A=M-D\n");
        out.write("D=M\n");
        out.write(at("THIS"));
        out.write("M=D\n");
        // restore ARG
        out.write(at("3"));
        out.write("D=A\n");
        out.write(at("R15"));
        out.write("A=M-D\n");
        out.write("D=M\n");
        out.write(at("ARG"));
        out.write("M=D\n");
        // restore LCL
        out.write(at("4"));
        out.write("D=A\n");
        out.write(at("R15"));
        out.write("A=M-D\n");
        out.write("D=M\n");
        out.write(at("LCL"));
        out.write("M=D\n");

        // return
        out.write(at("R14"));
        out.write("A=M\n");
        out.write("0;JMP\n");
    }

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
                out.write(at(file + "." + index));
                out.write("D=M\n");
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
                popD(out);
                out.write(at(file + "." + index));
                out.write("M=D\n");
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
        String yes = labelName();
        String no = labelName();
        String end = labelName();

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
        String yes = labelName();
        String no = labelName();
        String end = labelName();

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
        String yes = labelName();
        String no = labelName();
        String end = labelName();

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

    private String labelName() {
        String label = labelName(nextLabelIdx + "");
        nextLabelIdx++;
        return label;
    }

    private String labelName(String localName) {
        return file + "." + function + "$" + localName;
    }

    private String at(String address) {
        return "@" + address + "\n";
    }

    private String label(String name) {
        return "(" + name + ")\n";
    }
}
