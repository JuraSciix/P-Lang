package plang.interpreter;

import static plang.interpreter.Bytes.fetchUS;
import static plang.interpreter.Bytes.fetchUB;
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

        // Переносим данные из общей памяти в быструю
        System.arraycopy(arena, off, data, 0, size);

        while (state == STATE_RUN) {
            int opcode;
            switch (opcode = fetchUB(code, cp)) {
                case add: case sub:
                case mul: case div: case rem:
                case bit_and: case bit_or: case bit_xor: {
                    int r1 = fetchUB(code, cp + 1);
                    int r2 = fetchUB(code, cp + 2);
                    cp += 3;
                    switch (opcode) {
                        case add:     data[r1] = data[r1] + data[r2]; continue;
                        case sub:     data[r1] = data[r1] - data[r2]; continue;
                        case mul:     data[r1] = data[r1] * data[r2]; continue;
                        case div:     data[r1] = data[r1] / data[r2]; continue;
                        case rem:     data[r1] = data[r1] % data[r2]; continue;
                        case bit_and: data[r1] = data[r1] & data[r2]; continue;
                        case bit_or:  data[r1] = data[r1] | data[r2]; continue;
                        case bit_xor: data[r1] = data[r1] ^ data[r2]; continue;
                        default: throw new AssertionError();
                    }
                }

                case bit_inv: {
                    int i = fetchUB(code, cp + 1);
                    data[i] = ~data[i];
                    cp += 2;
                    continue;
                }

                case neg: {
                    int i = fetchUB(code, cp + 1);
                    data[i] = -data[i];
                    cp += 2;
                    continue;
                }

                case const_m1: case const_0:
                case const_1: case const_2: {
                    data[fetchUB(code, cp + 1)] = opcode - const_0;
                    cp += 2;
                    continue;
                }

                case load: {
                    data[fetchUB(code, cp + 3)] = pool[fetchUS(code, cp + 1)];
                    cp += 4;
                    continue;
                }

                case mov: {
                    data[fetchUB(code, cp + 2)] = data[fetchUB(code, cp + 1)];
                    cp += 3;
                    continue;
                }

                case cmp_eq: case cmp_ne: {
                    long lhs = data[fetchUB(code, cp + 1)];
                    long rhs = data[fetchUB(code, cp + 2)];
                    test = (lhs == rhs) ^ (opcode == cmp_ne);
                    cp += 3;
                    continue;
                }

                case cmp_lt: case cmp_le: {
                    long lhs = data[fetchUB(code, cp + 1)];
                    long rhs = data[fetchUB(code, cp + 2)];
                    test = (lhs < rhs) || (lhs == rhs) && (opcode == cmp_le);
                    cp += 3;
                    continue;
                }

                case cmp_gt: case cmp_ge: {
                    long lhs = data[fetchUB(code, cp + 1)];
                    long rhs = data[fetchUB(code, cp + 2)];
                    test = (lhs > rhs) || (lhs == rhs) && (opcode == cmp_ge);
                    cp += 3;
                    continue;
                }

                case jump: {
                    cp = fetchUS(code, cp + 1);
                    continue;
                }

                case jmp_z: case jmp_nz: {
                    cp += 3;
                    if (test ^ (opcode == jmp_z)) {
                        cp = fetchUS(code, cp - 2);
                    }
                    continue;
                }

                case ret:
                    state = STATE_RETURN;
                    cp += 1;
                    break;

                default: throw new AssertionError("Illegal opcode");
            }
        }

        // Переносим данные из быстрой памяти в общую
        System.arraycopy(data, 0, arena, off, size);

        return 0;
    }
}
