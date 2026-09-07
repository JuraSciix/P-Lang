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

    public void emitByte(int b) {
        code.add((byte) b);
    }

    public void emitShort(int s) {
        code.add((byte) s);
        code.add((byte) (s >> 8));
    }

    public void emitBB(int b1, int b2) {
        emitByte(b1);
        emitByte(b2);
    }

    public void emitBS(int b, int s) {
        emitByte(b);
        emitShort(s);
    }

    public void emitBSB(int b1, int s, int b2) {
        emitByte(b1);
        emitShort(s);
        emitByte(b2);
    }

    public void emitBBB(int b1, int b2, int b3) {
        emitByte(b1);
        emitByte(b2);
        emitByte(b3);
    }

    public void emitBBBB(int b1, int b2, int b3, int b4) {
        emitByte(b1);
        emitByte(b2);
        emitByte(b3);
        emitByte(b4);
    }

    public void setByte(int index, int value) {
        code.set(index, (byte) value);
    }

    public void setShort(int index, int value) {
        code.set(index, (byte) value);
        code.set(index + 1, (byte) (value >> 8));
    }

    public Mark mark(Mark prev) {
        return new Mark(top() - 2, prev);
    }

    public void close(Mark mark) {
        int t = top();
        for (Mark m = mark; m != null; m = m.prev) {
            setShort(m.index, t);
        }
    }
}
