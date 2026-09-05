package plang.utils;

import java.util.Arrays;

public class IntArrayList {
    private static final int INITIAL_CAPACITY = 16;

    private int[] array;
    private int count;

    public IntArrayList() {
        this(INITIAL_CAPACITY);
    }

    public IntArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        array = new int[INITIAL_CAPACITY];
    }

    public int size() {
        return count;
    }

    public void add(int element) {
        if (count >= array.length) {
            array = Arrays.copyOf(array, count * 2);
        }

        array[count++] = element;
    }

    public void set(int index, int element) {
        if (0 <= index && index < count) {
            array[index] = element;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public void and(int index, int mask) {
        if (0 <= index && index < count) {
            array[index] &= mask;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public void or(int index, int mask) {
        if (0 <= index && index < count) {
            array[index] |= mask;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public int get(int index) {
        if (0 <= index && index < count) {
            return array[index];
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    private String oob(int index) {
        return "Index: " + index + ". Count: " + size();
    }

    public int[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
