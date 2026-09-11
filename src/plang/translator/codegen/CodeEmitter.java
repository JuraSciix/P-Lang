package plang.translator.codegen;

import plang.utils.ByteArrayList;

public final class CodeEmitter {
    private final ByteArrayList code = new ByteArrayList();

    /**
     * Возвращает список байтов.
     *
     * @return Список байтов.
     */
    public ByteArrayList code() {
        return code;
    }

    /**
     * Возвращает указатель на следующий байткод.
     *
     * @return Указатель на следующий байткод.
     */
    public int top() {
        return code.size();
    }

    public void opcodeWithByteIndex(int opcode, int index) {
        code.add((byte) opcode);
        code.add((byte) index);
    }

    public void opcodeWithShortIndex(int opcode, int index) {
        code.add((byte) opcode);
        code.add((byte) index);
        code.add((byte) (index >> 8));
    }

    public void opcodeWithShortIndexAndByteIndex(int opcode, int index1, int index2) {
        code.add((byte) opcode);
        code.add((byte) index1);
        code.add((byte) (index1 >> 8));
        code.add((byte) index2);
    }

    public void opcodeWithDoubleByteIndex(int opcode, int index1, int index2) {
        code.add((byte) opcode);
        code.add((byte) index1);
        code.add((byte) index2);
    }

    public void opcodeWithTripleByteIndex(int opcode, int index1, int index2, int index3) {
        code.add((byte) opcode);
        code.add((byte) index1);
        code.add((byte) index2);
        code.add((byte) index3);
    }

    public void setShortIndex(int index, int value) {
        code.set(index, (byte) value);
        code.set(index + 1, (byte) (value >> 8));
    }
}
