package plang.interpreter;

public final class Bytes {
    private Bytes() {
        throw new AssertionError();
    }

    /**
     * Unsigned Byte.
     */
    public static int fetchUB(byte[] arr, int off) {
        return arr[off] & 0xff;
    }

    /**
     * Unsigned Short.
     */
    public static int fetchUS(byte[] arr, int off) {
        return arr[off] & 0xff | (arr[off + 1] & 0xff) << 8;
    }
}
