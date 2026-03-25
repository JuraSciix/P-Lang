package plang.utils;

import java.util.Arrays;

public final class ByteArrayBuilder {
    private static final int INITIAL_CAPACITY = 16;

    private byte[] array = new byte[INITIAL_CAPACITY];

    private int count = 0;

    public int size() {
        return count;
    }

    public void add(byte value) {
        if (count >= array.length) {
            array = Arrays.copyOf(array, count << 1);
        }

        array[count++] = value;
    }

    public void set(int index, byte value) {
        if (index < 0 || count <= index) {
            throw new IndexOutOfBoundsException();
        }

        array[index] = value;
    }

    public byte get(int index) {
        if (index < 0 || count <= index) {
            throw new IndexOutOfBoundsException();
        }

        return array[index];
    }

    public byte[] toArray() {
        return Arrays.copyOf(array, count);
    }
}
