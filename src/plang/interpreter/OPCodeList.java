package plang.interpreter;

public interface OPCodeList {
    int nop = 0;

    // Бинарные операции
    int add = 1;
    int sub = 2;
    int mul = 3;
    int div = 4;
    int rem = 5;
    int bit_and = 6;
    int bit_or = 7;
    int bit_xor = 8;

    // Унарные операции
    int bit_inv = 9;
    int neg = 10;

    int load = 11;
    int reset = 14;
    int mov   = 15;

    int cmp_eq = 16;
    int cmp_ne = 17;

    int jump = 18;
    int jmp_eq = 19;
    int jmp_ne = 20;

    int _return = 21;
    int leave = 22;
}
