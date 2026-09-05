package plang.utils;

import java.util.Arrays;

public class ByteArrayList {
    private static final int INITIAL_CAPACITY = 16;

    private byte[] array;
    private int count;

    public ByteArrayList() {
        this(INITIAL_CAPACITY);
    }

    public ByteArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        array = new byte[capacity];
    }

    public int size() {
        return count;
    }

    public void add(byte element) {
        if (count >= array.length) {
            array = Arrays.copyOf(array, count * 2);
        }

        array[count++] = element;
    }

    public void set(int index, byte element) {
        if (0 <= index && index < count) {
            array[index] = element;
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    public byte get(int index) {
        if (0 <= index && index < count) {
            return array[index];
        } else {
            throw new IndexOutOfBoundsException(oob(index));
        }
    }

    private String oob(int index) {
        return "Index: " + index + ". Count: " + size();
    }

    public byte[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
