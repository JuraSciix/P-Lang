package plang.interpreter;

import static plang.interpreter.OPCodeList.*;

public class BytecodeInterpreter {

    public int run(ExecuteBlock block) {
        byte[] code = block.code;
        int cp = 0;
        int[] registers = new int[256];
        int[] constantPool = block.constantPool;

        while (true) {
            switch (code[cp]) {
                case add: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] + registers[index2];
                    cp += 4;
                    break;
                }

                case sub: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] - registers[index2];
                    cp += 4;
                    break;
                }

                case mul: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] * registers[index2];
                    cp += 4;
                    break;
                }

                case div: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] / registers[index2];
                    cp += 4;
                    break;
                }

                case rem: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] % registers[index2];
                    cp += 4;
                    break;
                }

                case bit_and: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] & registers[index2];
                    cp += 4;
                    break;
                }

                case bit_or: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] | registers[index2];
                    cp += 4;
                    break;
                }

                case bit_xor: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    int index3 = code[cp + 3];
                    registers[index3] = registers[index1] ^ registers[index2];
                    cp += 4;
                    break;
                }

                case bit_inv: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    registers[index2] = ~registers[index1];
                    cp += 3;
                    break;
                }

                case neg: {
                    int index1 = code[cp + 1];
                    int index2 = code[cp + 2];
                    registers[index2] = -registers[index1];
                    cp += 3;
                    break;
                }

                case load: {
                    int constIndex = code[cp + 1];
                    int index = code[cp + 2];
                    registers[index] = constantPool[constIndex];
                    cp += 3;
                    break;
                }

                case mov: {
                    int index0 = code[cp + 1];
                    int index1 = code[cp + 2];
                    registers[index1] = registers[index0];
                    cp += 3;
                    break;
                }

                case _return: {
                    int index = code[cp + 1];
                    return registers[index];
                }

                case leave: {
                    return 0;
                }

                default:
                    throw new RuntimeException("Illegal opcode: " +
                            OPCodeInfo.opcodeString(code[cp]));
            }
        }
    }
}
