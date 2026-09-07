package plang.interpreter;

public final class Bytes {
    private Bytes() {
        throw new AssertionError();
    }

    public static int readUB(byte[] arr, int off) {
        return arr[off] & 0xff;
    }

    public static int read2UB(byte[] arr, int off) {
        return readUB(arr, off) | readUB(arr, off + 1) << 8;
    }
}
