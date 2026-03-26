package plang.translator;

import plang.utils.ByteArrayBuilder;

import java.util.Arrays;

public class Code {
    private static final int MAX_REGISTERS = 256;

    private final ByteArrayBuilder code = new ByteArrayBuilder();

    private final boolean[] registerStates = new boolean[MAX_REGISTERS];

    public Code() {
        Arrays.fill(registerStates, true);
    }

    public byte[] getByteArray() {
        return code.toArray();
    }

    public void emitPos(int pos) {

    }

    public void emit(int opcode) {
        code.add((byte) opcode);
    }

    public void emit1(int opcode, int argument) {
        code.add((byte) opcode);
        code.add((byte) argument);
    }

    public void emit2(int opcode, int a1, int a2) {
        code.add((byte) opcode);
        code.add((byte) a1);
        code.add((byte) a2);
    }

    public void emitUnary(int opcode, int index, int resultIndex) {
        code.add((byte) opcode);
        code.add((byte) index);
        code.add((byte) resultIndex);
    }

    public void emitBinary(int opcode, int index1, int index2, int resultIndex) {
        code.add((byte) opcode);
        code.add((byte) index1);
        code.add((byte) index2);
        code.add((byte) resultIndex);
    }

    public int allocReg() {
        int index = freeReg();
        captureReg(index);
        return index;
    }

    private int freeReg() {
        for (int i = 0; i < registerStates.length; i++) {
            if (registerStates[i]) {
                return i;
            }
        }

        throw new RuntimeException("No free registers");
    }

    private void captureReg(int index) {
        registerStates[index] = false;
    }

    public void releaseReg(int index) {
        registerStates[index] = true;
    }
}
