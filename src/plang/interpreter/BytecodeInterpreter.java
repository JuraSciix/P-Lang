package plang.interpreter;

import static plang.interpreter.Bytes.read2UB;
import static plang.interpreter.Bytes.readUB;
import static plang.interpreter.OPCodeList.*;

public final class BytecodeInterpreter {
    private static final int STATE_RUN = 0;
    private static final int STATE_RETURN = 1;

    // Быстрая память.
    private static final ThreadLocal<long[]> buffer = ThreadLocal.withInitial(() -> new long[256]);

    /**
     *
     * @param code Код программы
     * @param pool Пул констант.
     * @param cs   Откуда начнётся выполнение кода
     * @param arena Память (регистры)
     * @param off  Смещение в памяти.
     * @return Адрес возврата
     */
    public int run(byte[] code, long[] pool, int cs, long[] arena, int off, int size) {
        int cp = cs & 0xffff;
        long[] data = buffer.get();
        boolean test = false;
        int state = STATE_RUN;
        int returnAddress = 0;

        // Переносим данные из общей памяти в быструю
        System.arraycopy(arena, off, data, 0, size);

        while (state == STATE_RUN) {
            int opcode = readUB(code, cp);
            switch (opcode) {
                case add: case sub:
                case mul: case div: case rem:
                case bit_and: case bit_or: case bit_xor: {
                    long lhs = data[readUB(code, cp + 1)];
                    long rhs = data[readUB(code, cp + 2)];
                    int ri = readUB(code, cp + 3);
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
                    data[readUB(code, cp + 2)] = ~data[readUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case neg: {
                    data[readUB(code, cp + 2)] = -data[readUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case const_m1: case const_0:
                case const_1: case const_2: {
                    data[readUB(code, cp + 1)] = opcode - const_0;
                    cp += 2;
                    continue;
                }

                case load: {
                    data[readUB(code, cp + 3)] = pool[read2UB(code, cp + 1)];
                    cp += 4;
                    continue;
                }

                case mov: {
                    data[readUB(code, cp + 2)] = data[readUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case cmp_eq: case cmp_ne: {
                    long lhs = data[readUB(code, cp + 1)];
                    long rhs = data[readUB(code, cp + 2)];
                    test = (lhs == rhs) ^ (opcode == cmp_ne);
                    cp += 3;
                    continue;
                }

                case cmp_lt: case cmp_le: {
                    long lhs = data[readUB(code, cp + 1)];
                    long rhs = data[readUB(code, cp + 2)];
                    test = (lhs < rhs) || (lhs == rhs) && (opcode == cmp_le);
                    cp += 3;
                    continue;
                }

                case cmp_gt: case cmp_ge: {
                    long lhs = data[readUB(code, cp + 1)];
                    long rhs = data[readUB(code, cp + 2)];
                    test = (lhs > rhs) || (lhs == rhs) && (opcode == cmp_ge);
                    cp += 3;
                    continue;
                }

                case jump: {
                    cp = read2UB(code, cp + 1);
                    continue;
                }

                case jmp_z: case jmp_nz: {
                    cp += 3;
                    if (test ^ (opcode == jmp_nz)) {
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

        // Переносим данные из быстрой памяти в общую
        System.arraycopy(data, 0, arena, off, size);

        return returnAddress;
    }
}
