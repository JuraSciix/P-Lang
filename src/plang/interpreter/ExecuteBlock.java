package plang.interpreter;

public final class ExecuteBlock {
    public final byte[] code;
    public final long[] constantPool;

    public ExecuteBlock(byte[] code, long[] constantPool) {
        this.code = code;
        this.constantPool = constantPool;
    }
}
