package plang.translator.codegen;

public final class CodeData {
    public final byte[] code;
    public final long[] constantPool;
    public final int dataSize;

    public CodeData(byte[] code, long[] constantPool, int dataSize) {
        this.code = code;
        this.constantPool = constantPool;
        this.dataSize = dataSize;
    }
}
