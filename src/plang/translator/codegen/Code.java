package plang.translator.codegen;

public class Code {
    private final CodeEmitter emitter;

    public Code(CodeEmitter emitter) {
        this.emitter = emitter;
    }

    /**
     * Записывает код инструкции и указатель прыжка (jump target).
     *
     * @param opcode Код инструкции.
     * @param prev   Предыдущая метка.
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
     * @param mark   Хвост цепочки меток.
     * @param target Цель прыжка.
     */
    public void close(Mark mark, int target) {
        for (Mark m = mark; m != null; m = m.prev) {
            emitter.setShortIndex(m.index, target);
        }
    }
}
