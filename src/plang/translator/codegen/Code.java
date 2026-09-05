package plang.translator.codegen;

import java.util.Arrays;

public class Code {
    private static final int MAX_REGISTERS = 256;

    private final boolean[] freeness = new boolean[MAX_REGISTERS];

    public Code() {
        Arrays.fill(freeness, true);
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
