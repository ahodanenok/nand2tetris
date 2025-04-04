package ahodanenok.nand2tetris.vm.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;

import org.junit.jupiter.api.Test;

public class VmHackGeneratorTest {

    @Test
    public void testGenerate_Add() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().add(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            M=D+M
            """, out.toString());
    }

    @Test
    public void testGenerate_Sub() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().sub(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            M=M-D
            """, out.toString());
    }

    @Test
    public void testGenerate_Neg() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().neg(out);
        assertEquals("""
            @SP
            A=M-1
            M=-M
            """, out.toString());
    }

    @Test
    public void testGenerate_Eq() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().eq(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            D=M-D
            @LB_0
            D;JEQ
            @LB_1
            0;JMP
            (LB_0)
            @SP
            A=M-1
            M=-1
            @LB_2
            0;JMP
            (LB_1)
            @SP
            A=M-1
            M=0
            (LB_2)
            """, out.toString());
    }

    @Test
    public void testGenerate_Lt() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().lt(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            D=M-D
            @LB_0
            D;JLT
            @LB_1
            0;JMP
            (LB_0)
            @SP
            A=M-1
            M=-1
            @LB_2
            0;JMP
            (LB_1)
            @SP
            A=M-1
            M=0
            (LB_2)
            """, out.toString());
    }

    @Test
    public void testGenerate_Gt() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().gt(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            D=M-D
            @LB_0
            D;JGT
            @LB_1
            0;JMP
            (LB_0)
            @SP
            A=M-1
            M=-1
            @LB_2
            0;JMP
            (LB_1)
            @SP
            A=M-1
            M=0
            (LB_2)
            """, out.toString());
    }

    @Test
    public void testGenerate_And() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().and(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            M=D&M
            """, out.toString());
    }

    @Test
    public void testGenerate_Or() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().or(out);
        assertEquals("""
            @SP
            M=M-1
            A=M
            D=M
            @SP
            A=M-1
            M=D|M
            """, out.toString());
    }

    @Test
    public void testGenerate_Not() throws Exception {
        StringWriter out = new StringWriter();
        new VmHackGenerator().not(out);
        assertEquals("""
            @SP
            A=M-1
            M=!M
            """, out.toString());
    }
}
