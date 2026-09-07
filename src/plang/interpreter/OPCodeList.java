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

    int const_m1 = 11;
    int const_0 = 12;
    int const_1 = 13;
    int const_2 = 14;

    int load = 15;
    int reset = 16;
    int mov   = 17;

    int cmp_eq = 18;
    int cmp_ne = 19;
    int cmp_lt = 20;
    int cmp_ge = 21;
    int cmp_gt = 22;
    int cmp_le = 23;

    int jump = 24;
    int jmp_z = 25;
    int jmp_nz = 26;

    int _return = 27;
    int leave = 28;
}
