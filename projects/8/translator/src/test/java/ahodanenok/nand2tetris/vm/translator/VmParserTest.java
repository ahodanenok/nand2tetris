package ahodanenok.nand2tetris.vm.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class VmParserTest {

    @Test
    public void testParse_Empty() {
        VmParser parser = new VmParser("");
        assertFalse(parser.advance());
        assertNull(parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        push argument 0,   ARGUMENT,  0
        push local 1,      LOCAL,     1
        push pointer 2,    POINTER,   2
        push static 3,     STATIC,    3
        push temp 4,       TEMP,      4
        push that 5,       THAT,      5
        push this 6,       THIS,      6
    """)
    public void testParseCommand_Push(String code, VmSegment segment, int index) {
        VmParser parser = new VmParser(code);
        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(segment, parser.segment());
        assertEquals(index, parser.index());
        assertFalse(parser.advance());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
        pop argument 0,   ARGUMENT,  0
        pop local 1,      LOCAL,     1
        pop pointer 2,    POINTER,   2
        pop static 3,     STATIC,    3
        pop temp 4,       TEMP,      4
        pop that 5,       THAT,      5
        pop this 6,       THIS,      6
    """)
    public void testParseCommand_Pop(String code, VmSegment segment, int index) {
        VmParser parser = new VmParser(code);
        assertTrue(parser.advance());
        assertEquals(VmCommand.POP, parser.command());
        assertEquals(segment, parser.segment());
        assertEquals(index, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Add() {
        VmParser parser = new VmParser("add");
        assertTrue(parser.advance());
        assertEquals(VmCommand.ADD, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Sub() {
        VmParser parser = new VmParser("sub");
        assertTrue(parser.advance());
        assertEquals(VmCommand.SUB, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Neg() {
        VmParser parser = new VmParser("neg");
        assertTrue(parser.advance());
        assertEquals(VmCommand.NEG, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Eq() {
        VmParser parser = new VmParser("eq");
        assertTrue(parser.advance());
        assertEquals(VmCommand.EQ, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Gt() {
        VmParser parser = new VmParser("gt");
        assertTrue(parser.advance());
        assertEquals(VmCommand.GT, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Lt() {
        VmParser parser = new VmParser("lt");
        assertTrue(parser.advance());
        assertEquals(VmCommand.LT, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_And() {
        VmParser parser = new VmParser("and");
        assertTrue(parser.advance());
        assertEquals(VmCommand.AND, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Or() {
        VmParser parser = new VmParser("or");
        assertTrue(parser.advance());
        assertEquals(VmCommand.OR, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseCommand_Not() {
        VmParser parser = new VmParser("not");
        assertTrue(parser.advance());
        assertEquals(VmCommand.NOT, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
        assertFalse(parser.advance());
    }

    @Test
    public void testParseChunk_1() {
        VmParser parser = new VmParser("""
            push static 100
            push local 1
            neg
            push argument 4
            add
            push this 0
            lt
            pop that 3
        """);

        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(VmSegment.STATIC, parser.segment());
        assertEquals(100, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(VmSegment.LOCAL, parser.segment());
        assertEquals(1, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.NEG, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(VmSegment.ARGUMENT, parser.segment());
        assertEquals(4, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.ADD, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(VmSegment.THIS, parser.segment());
        assertEquals(0, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.LT, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.POP, parser.command());
        assertEquals(VmSegment.THAT, parser.segment());
        assertEquals(3, parser.index());

        assertFalse(parser.advance());
        assertNull(parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
    }

    @Test
    public void testParseChunk_2() {
        VmParser parser = new VmParser("""

            // Pushes and adds two constants.

            push constant 7
            push constant 8
            add

        """);

        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(VmSegment.CONSTANT, parser.segment());
        assertEquals(7, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.PUSH, parser.command());
        assertEquals(VmSegment.CONSTANT, parser.segment());
        assertEquals(8, parser.index());

        assertTrue(parser.advance());
        assertEquals(VmCommand.ADD, parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());

        assertFalse(parser.advance());
        assertNull(parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.index());
    }
}
