package plang.translator.codegen;

public final class CodeData {
    public final byte[] code;
    public final int[] constantPool;

    public CodeData(byte[] code, int[] constantPool) {
        this.code = code;
        this.constantPool = constantPool;
    }
}
