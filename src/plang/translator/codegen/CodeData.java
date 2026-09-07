package plang.translator.codegen;

public final class CodeData {
    public final byte[] code;
    public final long[] constantPool;

    public CodeData(byte[] code, long[] constantPool) {
        this.code = code;
        this.constantPool = constantPool;
    }
}
