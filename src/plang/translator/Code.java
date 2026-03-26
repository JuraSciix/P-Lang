package plang.translator;

import plang.utils.ByteArrayBuilder;
import plang.utils.IntArrayBuilder;

import java.util.Arrays;

public class Code {
    private static final int MAX_REGISTERS = 256;

    static final class Flow {
        final IntArrayBuilder codePointers = new IntArrayBuilder();
    }

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

    public int freeReg() {
        for (int i = 0; i < registerStates.length; i++) {
            if (registerStates[i]) {
                return i;
            }
        }

        throw new RuntimeException("No free registers");
    }

    public void captureReg(int index) {
        registerStates[index] = false;
    }

    public void releaseReg(int index) {
        registerStates[index] = true;
    }

    Flow newFlow(int opcode) {
        int cp = code.size();
        // Заготавливаем два байта под значение
        emit2(opcode, 0, 0);
        Flow flow = new Flow();
        flow.codePointers.add(cp);
        return flow;
    }

    void resolve(Flow flow) {
        int cp = code.size();
        int loByte = cp & 0xff;
        int hiByte = (cp >> 8) & 0xff;
        for (int i = 0; i < flow.codePointers.size(); i++) {
            code.set(flow.codePointers.get(i) + 1, (byte) loByte);
            code.set(flow.codePointers.get(i) + 2, (byte) hiByte);
        }
    }
}
