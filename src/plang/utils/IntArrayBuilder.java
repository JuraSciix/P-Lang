package plang.utils;

import java.util.Arrays;

public final class IntArrayBuilder {
    private static final int INITIAL_CAPACITY = 16;

    private int[] array = new int[INITIAL_CAPACITY];

    private int count = 0;

    public void add(int value) {
        if (count >= array.length) {
            array = Arrays.copyOf(array, count >> 1);
        }

        array[count++] = value;
    }

    public int[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
