package plang.interpreter;

import static plang.interpreter.OPCodeList.*;

public class BytecodeInterpreter {

    public int run(ExecuteBlock block) {
        byte[] code = block.code;
        int cp = 0;
        int[] registers = new int[256];
        int[] constantPool = block.constantPool;
        int flag = 0;

        while (0 <= cp && cp < code.length) {
            switch (Bytes.readUB(code, cp)) {
                case add: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] + registers[index2];
                    cp += 4;
                    break;
                }

                case sub: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] - registers[index2];
                    cp += 4;
                    break;
                }

                case mul: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] * registers[index2];
                    cp += 4;
                    break;
                }

                case div: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] / registers[index2];
                    cp += 4;
                    break;
                }

                case rem: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] % registers[index2];
                    cp += 4;
                    break;
                }

                case bit_and: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] & registers[index2];
                    cp += 4;
                    break;
                }

                case bit_or: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] | registers[index2];
                    cp += 4;
                    break;
                }

                case bit_xor: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    int index3 = Bytes.readUB(code, cp + 3);
                    registers[index3] = registers[index1] ^ registers[index2];
                    cp += 4;
                    break;
                }

                case bit_inv: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    registers[index2] = ~registers[index1];
                    cp += 3;
                    break;
                }

                case neg: {
                    int index1 = Bytes.readUB(code, cp + 1);
                    int index2 = Bytes.readUB(code, cp + 2);
                    registers[index2] = -registers[index1];
                    cp += 3;
                    break;
                }

                case load: {
                    int constIndex = Bytes.read2UB(code, cp + 1);
                    int index = Bytes.readUB(code, cp + 3);
                    registers[index] = constantPool[constIndex];
                    cp += 4;
                    break;
                }

                case reset: {
                    int index = Bytes.readUB(code, cp + 1);
                    registers[index] = 0;
                    cp += 2;
                    break;
                }

                case mov: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    registers[index1] = registers[index0];
                    cp += 3;
                    break;
                }

                case cmp_eq: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    flag = (registers[index0] == registers[index1]) ? 1 : 0;
                    cp += 3;
                    break;
                }

                case cmp_ne: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    flag = (registers[index0] != registers[index1]) ? 1 : 0;
                    cp += 3;
                    break;
                }

                case cmp_lt: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    flag = (registers[index0] < registers[index1]) ? 1 : 0;
                    cp += 3;
                    break;
                }

                case cmp_ge: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    flag = (registers[index0] >= registers[index1]) ? 1 : 0;
                    cp += 3;
                    break;
                }

                case cmp_gt: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    flag = (registers[index0] > registers[index1]) ? 1 : 0;
                    cp += 3;
                    break;
                }

                case cmp_le: {
                    int index0 = Bytes.readUB(code, cp + 1);
                    int index1 = Bytes.readUB(code, cp + 2);
                    flag = (registers[index0] <= registers[index1]) ? 1 : 0;
                    cp += 3;
                    break;
                }

                case jump: {
                    cp = Bytes.read2UB(code, cp + 1);
                    break;
                }

                case jmp_z: {
                    if (flag == 0) {
                        cp = Bytes.read2UB(code, cp + 1);
                        break;
                    }
                    cp += 3;
                    break;
                }

                case jmp_nz: {
                    if (flag == 1) {
                        cp = Bytes.read2UB(code, cp + 1);
                        break;
                    }
                    cp += 3;
                    break;
                }

                case _return: {
                    int index = Bytes.readUB(code, cp + 1);
                    return registers[index];
                }

                case leave: {
                    return 0;
                }

                default: {
                    int opcode = Bytes.readUB(code, cp);
                    throw new RuntimeException(
                            String.format("Illegal opcode: 0x%02x", opcode));
                }
            }
        }

        return -1;
    }
}
