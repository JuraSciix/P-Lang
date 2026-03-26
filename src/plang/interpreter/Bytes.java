package plang.interpreter;

class Bytes {

    static int read2ub(byte[] arr, int off) {
        return arr[off] & 0xff | (arr[off + 1] & 0xff) << 8;
    }
}
