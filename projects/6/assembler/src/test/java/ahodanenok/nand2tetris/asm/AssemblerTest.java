package ahodanenok.nand2tetris.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AssemblerTest {

    @ParameterizedTest
    @CsvSource(textBlock = """
        @0,      0000000000000000
        @1,      0000000000000001
        @532,    0000001000010100
        @32767,  0111111111111111
    """)
    public void testAssembleInstruction_A_Number(String code, String result) throws Exception {
        Assembler assembler = new Assembler();
        StringWriter out = new StringWriter();
        assembler.translate(code, out);
        assertEquals(result, out.toString());
    }

    public void testAssembleInstruction_A_Label(String code, String result) throws Exception {
        // todo
    }

    public void testAssembleInstruction_L(String code, String result) throws Exception {
        // todo
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        0,          1110101010000000
        1,          1110111111000000
        -1,         1110111010000000
        D,          1110001100000000
        A,          1110110000000000
        M,          1111110000000000
        !D,         1110001101000000
        !A,         1110110001000000
        !M,         1111110001000000
        -D,         1110001111000000
        -A,         1110110011000000
        -M,         1111110011000000
        D+1,        1110011111000000
        A+1,        1110110111000000
        M+1,        1111110111000000
        D-1,        1110001110000000
        A-1,        1110110010000000
        M-1,        1111110010000000
        D+A,        1110000010000000
        D+M,        1111000010000000
        D-A,        1110010011000000
        D-M,        1111010011000000
        A-D,        1110000111000000
        M-D,        1111000111000000
        D&A,        1110000000000000
        D&M,        1111000000000000
        D|A,        1110010101000000
        D|M,        1111010101000000
        M=D&A,      1110000000001000
        D=D&A,      1110000000010000
        DM=D&A,     1110000000011000
        A=D&A,      1110000000100000
        AM=D&A,     1110000000101000
        AD=D&A,     1110000000110000
        ADM=D&A,    1110000000111000
        D&A;JGT,    1110000000000001
        D&A;JEQ,    1110000000000010
        D&A;JGE,    1110000000000011
        D&A;JLT,    1110000000000100
        D&A;JNE,    1110000000000101
        D&A;JLE,    1110000000000110
        D&A;JMP,    1110000000000111
        D=A+1;JGE,  1110110111010011
    """)
    public void testAssembleInstruction_C(String code, String result) throws Exception {
        Assembler assembler = new Assembler();
        StringWriter out = new StringWriter();
        assembler.translate(code, out);
        assertEquals(result, out.toString());
    }
}
