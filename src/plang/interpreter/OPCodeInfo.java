package plang.interpreter;

import java.util.HashMap;
import java.util.Map;

import static plang.interpreter.OPCodeList.*;

public class OPCodeInfo {

    private static final Map<Integer, String> M = new HashMap<>();

    static {
        M.put(nop, "nop");

        M.put(add, "add");
        M.put(sub, "sub");
        M.put(mul, "mul");
        M.put(div, "div");
        M.put(rem, "rem");
        M.put(bit_and, "bit-and");
        M.put(bit_or, "bit-or");
        M.put(bit_xor, "bit-xor");
        M.put(bit_inv, "bit-inv");

        M.put(neg, "neg");

        M.put(load, "load");
        M.put(reset, "reset");
        M.put(mov, "mov");

        M.put(cmp_eq, "cmp-eq");
        M.put(cmp_ne, "cmp-ne");

        M.put(jump, "jump");
        M.put(jmp_eq, "jmp-eq");
        M.put(jmp_ne, "jmp-ne");

        M.put(_return, "ret");
        M.put(leave, "leave");
    }

    public static String opcodeString(int opcode) {
        // OPxAA
        return M.getOrDefault(opcode, String.format("OPx%02x", opcode));
    }

    public static int weight(int opcode) {
        if ((add <= opcode && opcode <= bit_xor)
                || (cmp_eq <= opcode && opcode <= cmp_ne)
                || (jmp_eq <= opcode && opcode <= jmp_ne)) {
            return 4;
        }

        if (opcode == neg || opcode == bit_inv
                || opcode == mov || opcode == load) {
            return 3;
        }

        if (opcode == jump || opcode == _return || opcode == reset) {
            return 2;
        }

        return 1;
    }
}
