package plang.interpreter;

import static plang.interpreter.Arithm.compare;
import static plang.interpreter.Bytes.read2UB;
import static plang.interpreter.Bytes.readUB;
import static plang.interpreter.OPCodeList.*;

public class BytecodeInterpreter {
    private static final int STATE_RUN = 0;
    private static final int STATE_RETURN = 1;

    public static long run(ExecuteBlock block) {
        byte[] code = block.code;
        int cp = 0;
        long[] registers = new long[256];
        long[] constantPool = block.constantPool;
        int flag = 0;
        int state = STATE_RUN;
        int returnAddress = 0;

        while (state == STATE_RUN) {
            switch (readUB(code, cp)) {
                case add: case sub:
                case mul: case div: case rem:
                case bit_and: case bit_or: case bit_xor: {
                    int index1 = readUB(code, cp + 1);
                    int index2 = readUB(code, cp + 2);
                    int index3 = readUB(code, cp + 3);
                    switch (readUB(code, cp)) {
                        case add:
                            registers[index3] = registers[index1] + registers[index2];
                            break;
                        case sub:
                            registers[index3] = registers[index1] - registers[index2];
                            break;
                        case mul:
                            registers[index3] = registers[index1] * registers[index2];
                            break;
                        case div:
                            registers[index3] = registers[index1] / registers[index2];
                            break;
                        case rem:
                            registers[index3] = registers[index1] % registers[index2];
                            break;
                        case bit_and:
                            registers[index3] = registers[index1] & registers[index2];
                            break;
                        case bit_or:
                            registers[index3] = registers[index1] | registers[index2];
                            break;
                        case bit_xor:
                            registers[index3] = registers[index1] ^ registers[index2];
                            break;
                    }
                    cp += 4;
                    continue;
                }

                case bit_inv: {
                    int index1 = readUB(code, cp + 1);
                    int index2 = readUB(code, cp + 2);
                    registers[index2] = ~registers[index1];
                    cp += 3;
                    continue;
                }

                case neg: {
                    int index1 = readUB(code, cp + 1);
                    int index2 = readUB(code, cp + 2);
                    registers[index2] = -registers[index1];
                    cp += 3;
                    continue;
                }

                case const_m1: case const_0:
                case const_1: case const_2: {
                    int index = readUB(code, cp + 1);
                    registers[index] = readUB(code, cp) - const_0;
                    cp += 2;
                    continue;
                }

                case load: {
                    int constIndex = read2UB(code, cp + 1);
                    int index = readUB(code, cp + 3);
                    registers[index] = constantPool[constIndex];
                    cp += 4;
                    continue;
                }

                case reset: {
                    int index = readUB(code, cp + 1);
                    registers[index] = 0L;
                    cp += 2;
                    continue;
                }

                case mov: {
                    int index0 = readUB(code, cp + 1);
                    int index1 = readUB(code, cp + 2);
                    registers[index1] = registers[index0];
                    cp += 3;
                    continue;
                }

                case cmp_eq:case cmp_ne:
                case cmp_le: case cmp_lt:
                case cmp_ge: case cmp_gt: {
                    int opcode = readUB(code, cp);
                    int index0 = readUB(code, cp + 1);
                    int index1 = readUB(code, cp + 2);
                    flag = (opcode == cmp_ne) ^ compare(registers[index0], registers[index1],
                            opcode == cmp_ne || opcode == cmp_eq || opcode == cmp_ge || opcode == cmp_le,
                            opcode == cmp_ge || opcode == cmp_gt,
                            opcode == cmp_le || opcode == cmp_lt) ? 1 : 0;
                    cp += 3;
                    continue;
                }

                case jump: {
                    cp = read2UB(code, cp + 1);
                    continue;
                }

                case jmp_z:
                case jmp_nz: {
                    cp += 3;
                    if ((readUB(code, cp) == jmp_nz) ^ (flag == 0)) {
                        cp = read2UB(code, cp - 2);
                    }
                    continue;
                }

                case ret:
                    state = STATE_RETURN;
                    returnAddress = readUB(code, cp + 1);
                    break;

                default: throw new AssertionError("Illegal opcode");
            }
        }

        return registers[returnAddress];
    }
}
