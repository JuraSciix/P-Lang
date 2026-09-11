package plang.translator.codegen;

import java.util.BitSet;

public class Arena {
    public static final int LIMIT = 256;

    private final BitSet field = new BitSet(LIMIT);

    public Arena() {
        field.set(0, LIMIT);
    }

    public int acquire() {
        int index = findFree();
        field.clear(index);
        return index;
    }

    private int findFree() {
        return field.nextSetBit(0);
    }

    public void release(int index) {
        field.set(index);
    }
}
