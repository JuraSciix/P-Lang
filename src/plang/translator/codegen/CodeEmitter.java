package plang.translator.codegen;

import plang.translator.TranslatorException;
import plang.utils.ByteArrayList;

public final class CodeEmitter {
    private static final int MAX_CAPACITY = 65536;

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

    private void ensureCapacity(int cap) {
        if (MAX_CAPACITY - code.size() < cap) {
            throw new TranslatorException("Too big code");
        }
    }

    public void opcodeWithByteIndex(int opcode, int index) {
        ensureCapacity(2);
        code.add((byte) opcode);
        code.add((byte) index);
    }

    public void opcodeWithShortIndex(int opcode, int index) {
        ensureCapacity(3);
        code.add((byte) opcode);
        code.add((byte) index);
        code.add((byte) (index >> 8));
    }

    public void opcodeWithShortIndexAndByteIndex(int opcode, int index1, int index2) {
        ensureCapacity(4);
        code.add((byte) opcode);
        code.add((byte) index1);
        code.add((byte) (index1 >> 8));
        code.add((byte) index2);
    }

    public void opcodeWithDoubleByteIndex(int opcode, int index1, int index2) {
        ensureCapacity(3);
        code.add((byte) opcode);
        code.add((byte) index1);
        code.add((byte) index2);
    }

    public void opcodeWithTripleByteIndex(int opcode, int index1, int index2, int index3) {
        ensureCapacity(4);
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
