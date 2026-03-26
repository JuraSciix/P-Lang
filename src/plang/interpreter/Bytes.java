package plang.interpreter;

class Bytes {

    static int readUB(byte[] arr, int off) {
        return arr[off] & 0xff;
    }

    static int read2UB(byte[] arr, int off) {
        return readUB(arr, off) | readUB(arr, off + 1) << 8;
    }
}
