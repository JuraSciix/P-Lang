package plang.translator.codegen;

import plang.translator.TranslatorException;

import java.util.BitSet;

public final class CodeArena {
    private static final int LIMIT = 256;

    private final BitSet field;
    private int limit;

    public CodeArena() {
        field = new BitSet(LIMIT);
        field.set(0, LIMIT);
    }

    public int acquire() {
        int index = field.nextSetBit(0);
        if (index < 0) {
            throw new TranslatorException("No more free indexes");
        }
        field.clear(index);
        limit = Math.max(index, limit);
        return index;
    }

    public void release(int index) {
        assert !field.get(index) : "Index " + index + " already was free";
        field.set(index);
    }

    /**
     * Возвращает максимальный задействованный регистр.
     * Можно использовать как "размер памяти" программы.
     */
    public int limit() { return limit; }

    // Особые операции, нарушающие естественную последовательность операций.
    // Необходимо для инструкции ret, которая нагло забирает себе нулевой регистр.

    public boolean test(int index) {
        return field.get(index);
    }

    public void reset(int index, boolean value) {
        field.set(index, value);
    }
}
