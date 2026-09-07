package plang.interpreter;

import static plang.interpreter.Arithm.compare;
import static plang.interpreter.Bytes.read2UB;
import static plang.interpreter.Bytes.readUB;
import static plang.interpreter.OPCodeList.*;

public class BytecodeInterpreter {
    private static final int STATE_RUN = 0;
    private static final int STATE_RETURN = 1;

    /**
     *
     * @param code Код программы
     * @param pool Пул констант.
     * @param cs Откуда начнётся выполнение кода
     * @param data Память (регистры)
     * @param off Смещение в памяти.
     * @return Адрес возврата
     */
    public static int run(byte[] code, long[] pool, int cs, long[] data, int off) {
        int cp = cs & 0xffff;
        int flag = 0;
        int state = STATE_RUN;
        int returnAddress = 0;

        while (state == STATE_RUN) {
            int opcode = readUB(code, cp);
            switch (opcode) {
                case add: case sub:
                case mul: case div: case rem:
                case bit_and: case bit_or: case bit_xor: {
                    long lhs = data[off + readUB(code, cp + 1)];
                    long rhs = data[off + readUB(code, cp + 2)];
                    int ri = off + readUB(code, cp + 3);
                    cp += 4;
                    switch (opcode) {
                        case add:     data[ri] = lhs + rhs; continue;
                        case sub:     data[ri] = lhs - rhs; continue;
                        case mul:     data[ri] = lhs * rhs; continue;
                        case div:     data[ri] = lhs / rhs; continue;
                        case rem:     data[ri] = lhs % rhs; continue;
                        case bit_and: data[ri] = lhs & rhs; continue;
                        case bit_or:  data[ri] = lhs | rhs; continue;
                        case bit_xor: data[ri] = lhs ^ rhs; continue;
                        default: throw new AssertionError();
                    }
                }

                case bit_inv: {
                    data[off + readUB(code, cp + 2)] = ~data[off + readUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case neg: {
                    data[off + readUB(code, cp + 2)] = -data[off + readUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case const_m1: case const_0:
                case const_1: case const_2: {
                    data[off + readUB(code, cp + 1)] = opcode - const_0;
                    cp += 2;
                    continue;
                }

                case load: {
                    data[off + readUB(code, cp + 3)] = pool[off + read2UB(code, cp + 1)];
                    cp += 4;
                    continue;
                }

                case mov: {
                    data[off + readUB(code, cp + 2)] = data[off + readUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case cmp_eq:case cmp_ne:
                case cmp_le: case cmp_lt:
                case cmp_ge: case cmp_gt: {
                    flag = (opcode == cmp_ne) ^ compare(
                            data[off + readUB(code, cp + 1)],
                            data[off + readUB(code, cp + 2)],
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
                    if ((opcode == jmp_nz) ^ (flag == 0)) {
                        cp = read2UB(code, cp - 2);
                    }
                    continue;
                }

                case ret:
                    state = STATE_RETURN;
                    returnAddress = off + readUB(code, cp + 1);
                    break;

                default: throw new AssertionError("Illegal opcode");
            }
        }

        return returnAddress;
    }
}
