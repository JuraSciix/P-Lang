package plang.translator.codegen;

import plang.translator.TranslatorException;

import java.util.BitSet;

public final class CodeArena {
    private static final int LIMIT = 256;

    private final BitSet field;

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
        return index;
    }

    public void release(int index) {
        assert !field.get(index) : "Index " + index + " already was free";
        field.set(index);
    }
}
