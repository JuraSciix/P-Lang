package plang.translator;

class OPCodes {

    static int negate(int opcode) {
        return ((opcode - 1) ^ 1) + 1;
    }
}
