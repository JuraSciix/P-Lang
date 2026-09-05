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

    public void emit(int opcode) {
        code.add((byte) opcode);
    }

    public void emit1(int opcode, int argument) {
        emitUB(opcode);
        emitUB(argument);
    }

    public void emitWith2UB(int opcode, int argument) {
        emitUB(opcode);
        emit2UB(argument);
    }

    public void emit2UBWithUB(int opcode, int x, int y) {
        emitUB(opcode);
        emit2UB(x);
        emitUB(y);
    }

    public void emit2(int opcode, int a1, int a2) {
        emitUB(opcode);
        emitUB(a1);
        emitUB(a2);
    }

    public void emitUnary(int opcode, int index, int resultIndex) {
        emitUB(opcode);
        emitUB(index);
        emitUB(resultIndex);
    }

    public void emitBinary(int opcode, int index1, int index2, int resultIndex) {
        emitUB(opcode);
        emitUB(index1);
        emitUB(index2);
        emitUB(resultIndex);
    }

    public void emit2UB(int value) {
        emitUB(value);
        emitUB(value >> 8);
    }

    public void emitUB(int value) {
        code.add((byte) value);
    }

    public void setUB(int pc, int value) {
        code.set(pc, (byte) value);
    }

    public void set2UB(int pc, int value) {
        setUB(pc, value);
        setUB(pc + 1, value >> 8);
    }
}
