package ahodanenok.nand2tetris.vm.translator;

public enum VmCommand {

    PUSH, POP,
    ADD, SUB, NEG,
    EQ, GT, LT,
    AND, OR, NOT,
    LABEL, IF_GOTO, GOTO;
}
