// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

(SCAN)
@KBD
D=M
@BLACK
D;JNE
@WHITE
D;JEQ
@SCAN
0;JMP

(BLACK)
@R6
M=-1
@COLOR
0;JMP

(WHITE)
@R6
M=0
@COLOR
0;JMP

(COLOR)
// R5 = @SCREEN
@SCREEN
D=A
@R5
M=D
(COLOR-LOOP)
// if (R5 == @KBD) goto @SCAN
@R5
D=M
@KBD
D=D-A
@SCAN
D;JEQ
// M[R5] = M[R6]
@R6
D=M
@R5
A=M
M=D
// R5 = R5 + 1
@R5
M=M+1
@COLOR-LOOP
0;JMP
