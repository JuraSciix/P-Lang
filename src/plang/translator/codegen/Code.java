package plang.translator.codegen;

import java.util.Arrays;

public class Code {
    private static final int MAX_REGISTERS = 256;

    private final boolean[] registerStates = new boolean[MAX_REGISTERS];

    public Code() {
        Arrays.fill(registerStates, true);
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


    public void emitPos(int pos) {

    }
}
