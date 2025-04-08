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
        checkNoMoreCommands(parser);
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
        checkPushPop(parser, VmCommand.PUSH, segment, index);
        checkNoMoreCommands(parser);
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
        checkPushPop(parser, VmCommand.POP, segment, index);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Add() {
        VmParser parser = new VmParser("add");
        checkCommand(parser, VmCommand.ADD);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Sub() {
        VmParser parser = new VmParser("sub");
        checkCommand(parser, VmCommand.SUB);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Neg() {
        VmParser parser = new VmParser("neg");
        checkCommand(parser, VmCommand.NEG);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Eq() {
        VmParser parser = new VmParser("eq");
        checkCommand(parser, VmCommand.EQ);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Gt() {
        VmParser parser = new VmParser("gt");
        checkCommand(parser, VmCommand.GT);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Lt() {
        VmParser parser = new VmParser("lt");
        checkCommand(parser, VmCommand.LT);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_And() {
        VmParser parser = new VmParser("and");
        checkCommand(parser, VmCommand.AND);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Or() {
        VmParser parser = new VmParser("or");
        checkCommand(parser, VmCommand.OR);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Not() {
        VmParser parser = new VmParser("not");
        checkCommand(parser, VmCommand.NOT);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Label() {
        VmParser parser = new VmParser("label TEST");
        checkLabel(parser, VmCommand.LABEL, "TEST");
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Goto() {
        VmParser parser = new VmParser("goto THERE");
        checkLabel(parser, VmCommand.GOTO, "THERE");
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_IfGoto() {
        VmParser parser = new VmParser("if-goto OK");
        checkLabel(parser, VmCommand.IF_GOTO, "OK");
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Function() {
        VmParser parser = new VmParser("function Circle.area 1");
        checkFunction(parser, "Circle.area", 1);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Call() {
        VmParser parser = new VmParser("call Math.sum 2");
        checkCall(parser, "Math.sum", 2);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseCommand_Return() {
        VmParser parser = new VmParser("return");
        checkCommand(parser, VmCommand.RETURN);
        checkNoMoreCommands(parser);
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

        checkPushPop(parser, VmCommand.PUSH, VmSegment.STATIC, 100);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.LOCAL, 1);
        checkCommand(parser, VmCommand.NEG);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.ARGUMENT, 4);
        checkCommand(parser, VmCommand.ADD);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.THIS, 0);
        checkCommand(parser, VmCommand.LT);
        checkPushPop(parser, VmCommand.POP, VmSegment.THAT, 3);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseChunk_2() {
        VmParser parser = new VmParser("""

            // Pushes and adds two constants.

            push constant 7
            push constant 8
            add

        """);

        checkPushPop(parser, VmCommand.PUSH, VmSegment.CONSTANT, 7);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.CONSTANT, 8);
        checkCommand(parser, VmCommand.ADD);
        checkNoMoreCommands(parser);
    }

    @Test
    public void testParseChunk_3() {
        VmParser parser = new VmParser("""

            	push constant 0
                pop local 0         // sum = 0
            label LOOP
                push argument 0
                push local 0
                add
                pop local 0	        // sum = sum + n
                push argument 0
                push constant 1
                sub
                pop argument 0      // n--
                push argument 0
                if-goto LOOP        // if n > 0, goto LOOP
                push local 0        // else, pushes sum to the stack's top

        """);

        checkPushPop(parser, VmCommand.PUSH, VmSegment.CONSTANT, 0);
        checkPushPop(parser, VmCommand.POP, VmSegment.LOCAL, 0);
        checkLabel(parser, VmCommand.LABEL, "LOOP");
        checkPushPop(parser, VmCommand.PUSH, VmSegment.ARGUMENT, 0);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.LOCAL, 0);
        checkCommand(parser, VmCommand.ADD);
        checkPushPop(parser, VmCommand.POP, VmSegment.LOCAL, 0);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.ARGUMENT, 0);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.CONSTANT, 1);
        checkCommand(parser, VmCommand.SUB);
        checkPushPop(parser, VmCommand.POP, VmSegment.ARGUMENT, 0);
        checkPushPop(parser, VmCommand.PUSH, VmSegment.ARGUMENT, 0);
        checkLabel(parser, VmCommand.IF_GOTO, "LOOP");
        checkPushPop(parser, VmCommand.PUSH, VmSegment.LOCAL, 0);
        checkNoMoreCommands(parser);
    }

    //@Test
    public void testParseChunk_4() {
        VmParser parser = new VmParser("""
            function Main.fibonacci 0
                push argument 0
                push constant 2
                lt
                if-goto N_LT_2
                goto N_GE_2
            label N_LT_2               // if n < 2 returns n
                push argument 0
                return
            label N_GE_2               // if n >= 2 returns fib(n - 2) + fib(n - 1)
                push argument 0
                push constant 2
                sub
                call Main.fibonacci 1  // computes fib(n - 2)
                push argument 0
                push constant 1
                sub
                call Main.fibonacci 1  // computes fib(n - 1)
                add                    // returns fib(n - 1) + fib(n - 2)
                return
        """);
    }

    private void checkLabel(VmParser parser, VmCommand command, String label) {
        assertTrue(parser.advance());
        assertEquals(command, parser.command());
        assertEquals(label, parser.label());
        assertNull(parser.segment());
        assertEquals(-1, parser.number());
    }

    private void checkFunction(VmParser parser, String name, int variableCount) {
        assertTrue(parser.advance());
        assertEquals(VmCommand.FUNCTION, parser.command());
        assertEquals(name, parser.label());
        assertNull(parser.segment());
        assertEquals(variableCount, parser.number());
    }

    private void checkCall(VmParser parser, String name, int argumentCount) {
        assertTrue(parser.advance());
        assertEquals(VmCommand.CALL, parser.command());
        assertEquals(name, parser.label());
        assertNull(parser.segment());
        assertEquals(argumentCount, parser.number());
    }

    private void checkCommand(VmParser parser, VmCommand command) {
        assertTrue(parser.advance());
        assertEquals(command, parser.command());
        assertNull(parser.label());
        assertNull(parser.segment());
        assertEquals(-1, parser.number());
    }

    private void checkPushPop(VmParser parser, VmCommand command, VmSegment segment, int index) {
        assertTrue(parser.advance());
        assertEquals(command, parser.command());
        if (segment != null) {
            assertEquals(segment, parser.segment());
        } else {
            assertNull(parser.segment());
        }
        assertEquals(index, parser.number());
        assertNull(parser.label());
    }

    private void checkNoMoreCommands(VmParser parser) {
        assertFalse(parser.advance());
        assertNull(parser.command());
        assertNull(parser.segment());
        assertEquals(-1, parser.number());
        assertNull(parser.label());
    }
}
