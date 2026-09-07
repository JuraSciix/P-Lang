package plang.translator.codegen;

import plang.utils.ByteArrayList;

public class CodeEmitter {
    private final ByteArrayList code = new ByteArrayList();

    public int top() {
        return code.size();
    }

    public byte[] getCodeArray() {
        return code.toArray();
    }

    public void emit(int b) {
        code.add((byte) b);
    }

    public void emit2(int s) {
        code.add((byte) s);
        code.add((byte) (s >> 8));
    }

    public void emitBB(int b1, int b2) {
        emit(b1);
        emit(b2);
    }

    public void emitBS(int b, int s) {
        emit(b);
        emit2(s);
    }

    public void emitBSB(int b1, int s, int b2) {
        emit(b1);
        emit2(s);
        emit(b2);
    }

    public void emitBBB(int b1, int b2, int b3) {
        emit(b1);
        emit(b2);
        emit(b3);
    }

    public void emitBBBB(int b1, int b2, int b3, int b4) {
        emit(b1);
        emit(b2);
        emit(b3);
        emit(b4);
    }

    public void b(int index, int value) {
        code.set(index, (byte) value);
    }

    public void s(int index, int value) {
        code.set(index, (byte) value);
        code.set(index + 1, (byte) (value >> 8));
    }
}
