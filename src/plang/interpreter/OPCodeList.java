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

    int load = 12;
    int reset = 14;
    int mov   = 15;

    int cmp_eq = 16;
    int cmp_ne = 17;
    int cmp_lt = 18;
    int cmp_ge = 19;
    int cmp_gt = 20;
    int cmp_le = 21;


    int jump = 22;
    int jmp_z = 23;
    int jmp_nz = 24;

    int _return = 29;
    int leave = 30;
}
