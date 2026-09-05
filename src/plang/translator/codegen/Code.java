package plang.translator.codegen;

import plang.utils.ByteArrayList;

import java.util.Arrays;

public class Code {
    private static final int MAX_REGISTERS = 256;

    public static final class Jump {
        final int cp;

        Jump(int cp) {
            this.cp = cp;
        }
    }

    private final ByteArrayList code = new ByteArrayList();

    private final boolean[] registerStates = new boolean[MAX_REGISTERS];

    public Code() {
        Arrays.fill(registerStates, true);
    }

    public byte[] getByteArray() {
        return code.toArray();
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

    public int getCodePoint() {
        return code.size();
    }

    public Jump jump(int opcode) {
        int cp = getCodePoint();
        emitWith2UB(opcode, 0); // Резервируем место нулем
        return new Jump(cp);
    }

    public void resolveJump(Jump jump) {
        resolveJump(jump, getCodePoint());
    }

    public void resolveJump(Jump jump, int cp) {
        code.set(jump.cp + 1, (byte) cp);
        code.set(jump.cp + 2, (byte) (cp >> 8));
    }

    public void emitPos(int pos) {

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
}
