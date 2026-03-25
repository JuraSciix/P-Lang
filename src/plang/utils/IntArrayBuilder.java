package plang.utils;

import java.util.Arrays;

public final class IntArrayBuilder {
    private static final int INITIAL_CAPACITY = 16;

    private int[] array = new int[INITIAL_CAPACITY];

    private int count = 0;

    public int size() {
        return count;
    }

    public void add(int value) {
        if (count >= array.length) {
            array = Arrays.copyOf(array, count << 1);
        }

        array[count++] = value;
    }

    public void set(int index, int value) {
        if (index < 0 || count <= index) {
            throw new IndexOutOfBoundsException();
        }

        array[index] = value;
    }

    public int get(int index) {
        if (index < 0 || count <= index) {
            throw new IndexOutOfBoundsException();
        }

        return array[index];
    }

    public int[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
