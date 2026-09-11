package plang.translator.codegen;

import java.util.Arrays;

public class Code {
    private static final int MAX_REGISTERS = 256;

    private final CodeEmitter emitter;
    private final boolean[] freeness = new boolean[MAX_REGISTERS];

    public Code(CodeEmitter emitter) {
        this.emitter = emitter;
        Arrays.fill(freeness, true);
    }

    /**
     * Записывает код инструкции и указатель прыжка (jump target).
     *
     * @param opcode Код инструкции.
     * @param prev Предыдущая метка.
     * @return Метку на указатель прыжка.
     */
    public Mark jump(int opcode, Mark prev) {
        int jumpTarget = emitter.top() + 1;
        emitter.opcodeWithShortIndex(opcode, 0);
        return new Mark(jumpTarget, prev);
    }

    public void close(Mark mark) {
        close(mark, emitter.top());
    }

    /**
     * Устанавливает цепочке меток указателей прыжка цель.
     *
     * @param mark Хвост цепочки меток.
     * @param target Цель прыжка.
     */
    public void close(Mark mark, int target) {
        for (Mark m = mark; m != null; m = m.prev) {
            emitter.setShortIndex(m.index, target);
        }
    }

    public int acquire() {
        int index = findFree();
        freeness[index] = false;
        return index;
    }

    private int findFree() {
        for (int i = 0; i < freeness.length; i++) {
            if (freeness[i]) {
                return i;
            }
        }

        throw new AssertionError("No free registers");
    }

    public void release(int index) {
        if (freeness[index]) {
            throw new AssertionError("Index " + index + " was already free");
        }
        freeness[index] = true;
    }
}
