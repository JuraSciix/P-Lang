package plang.utils;

import java.util.Arrays;

public class LongArrayList {
    private static final int INITIAL_CAPACITY = 16;

    private long[] array;
    private int count;

    public LongArrayList() {
        this(INITIAL_CAPACITY);
    }

    public LongArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        array = new long[INITIAL_CAPACITY];
    }

    public int size() {
        return count;
    }

    public void add(long element) {
        if (count >= array.length) {
            array = Arrays.copyOf(array, array.length * 2);
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

    public long get(int index) {
        if (0 <= index && index < count) {
            return array[index];
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    private String oob(int index) {
        return "Index: " + index + ". Count: " + size();
    }

    public long[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
