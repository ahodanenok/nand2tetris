package ahodanenok.nand2tetris.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ParserTest {

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 123, 6343, 32768 })
    public void testParseInstruction_A_Number(int number) {
        Parser parser = new Parser("@" + number);
        assertTrue(parser.advance());
        assertEquals(InstructionType.A, parser.instType());
        assertEquals(number, parser.number());
        assertNull(parser.symbol());
        assertNull(parser.comp());
        assertNull(parser.dest());
        assertNull(parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "_", ":", "$", ".", "abc", "XY", "foo.bar", "hello_world$", "_:$.", "q1", "i"
    })
    public void testParseInstruction_A_Symbol(String symbol) {
        Parser parser = new Parser("@" + symbol);
        assertTrue(parser.advance());
        assertEquals(InstructionType.A, parser.instType());
        assertEquals(symbol, parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.comp());
        assertNull(parser.dest());
        assertNull(parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "LOOP", "A1", "_here", "$return$", "$$$", "fn.start"
    })
    public void testParseInstruction_L(String symbol) {
        Parser parser = new Parser("(" + symbol + ")");
        assertTrue(parser.advance());
        assertEquals(InstructionType.L, parser.instType());
        assertEquals(symbol, parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.comp());
        assertNull(parser.dest());
        assertNull(parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "0", "1", "-1", "D", "A", "M"
    })
    public void testParseInstruction_C_Comp(String comp) {
        Parser parser = new Parser(comp);
        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals(comp, parser.comp());
        assertNull(parser.dest());
        assertNull(parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        0;JGT,     0,   JGT
        D+A;JEQ,   D+A, JEQ
        -D;JGE,    -D,  JGE
        -1;JLT,    -1,  JLT
        !A;JNE,    !A,  JNE
        D-1;JLE,   D-1, JLE
        A;JMP,     A,   JMP
    """)
    public void testParseInstruction_C_CompJump(String code, String comp, String jump) {
        Parser parser = new Parser(code);
        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.dest());
        assertEquals(comp, parser.comp());
        assertEquals(jump, parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        M=-1,     M,   -1
        D=D|A,    D,   D|A
        MD=A+1,   MD,  A+1
        A=D+M,    A,   D+M
        AM=D-1,   AM,  D-1
        AD=!A,    AD,  !A
        ADM=1,    ADM, 1
    """)
    public void testParseInstruction_C_DestComp(String code, String dest, String comp) {
        Parser parser = new Parser(code);
        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals(dest, parser.dest());
        assertEquals(comp, parser.comp());
        assertNull(parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        A=1;JGT,       A,   1,   JGT
        D=0;JEQ,       D,   0,   JEQ
        AD=D+A;JGE,    AD,  D+A, JGE
        D=A;JLT,       D,   A,   JLT
        M=D+1;JNE,     M,   D+1, JNE
        ADM=D&M;JLE,   ADM, D&M, JLE
        MD=A-1;JMP,    MD,  A-1, JMP
    """)
    public void testParseInstruction_C_DestCompJump(String code, String dest, String comp, String jump) {
        Parser parser = new Parser(code);
        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals(dest, parser.dest());
        assertEquals(comp, parser.comp());
        assertEquals(jump, parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "// comment\nD=1;JMP",
        "D=1;JMP// A=0",
        "// 123\nD=1;JMP\n// 456"
    })
    public void testSkipComments(String code) {
        Parser parser = new Parser(code);
        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals("D", parser.dest());
        assertEquals("1", parser.comp());
        assertEquals("JMP", parser.jump());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @ValueSource(strings = { "  A=D", "A=D   ", "   A=D  " })
    public void testSkipWhitespaces(String code) {
        Parser parser = new Parser(code);
        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals("A", parser.dest());
        assertEquals("D", parser.comp());
        assertNull(parser.jump());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseChunk() {
        String code = """
            // D = R0 - R1
            @R0
            D=M
            @R1
            D=D-M
            // If (D > 0) goto ITSR0
            @ITSR0
            D;JGT
        """;
        Parser parser = new Parser(code);

        assertTrue(parser.advance());
        assertEquals(InstructionType.A, parser.instType());
        assertEquals("R0", parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.dest());
        assertNull(parser.comp());
        assertNull(parser.jump());

        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals("D", parser.dest());
        assertEquals("M", parser.comp());
        assertNull(parser.jump());

        assertTrue(parser.advance());
        assertEquals(InstructionType.A, parser.instType());
        assertEquals("R1", parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.dest());
        assertNull(parser.comp());
        assertNull(parser.jump());

        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertEquals("D", parser.dest());
        assertEquals("D-M", parser.comp());
        assertNull(parser.jump());

        assertTrue(parser.advance());
        assertEquals(InstructionType.A, parser.instType());
        assertEquals("ITSR0", parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.dest());
        assertNull(parser.comp());
        assertNull(parser.jump());

        assertTrue(parser.advance());
        assertEquals(InstructionType.C, parser.instType());
        assertNull(parser.symbol());
        assertEquals(-1, parser.number());
        assertNull(parser.dest());
        assertEquals("D", parser.comp());
        assertEquals("JGT", parser.jump());

        assertFalse(parser.advance());
    }
}
