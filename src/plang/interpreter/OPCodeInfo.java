package plang.interpreter;

import static plang.interpreter.OPCodeList.*;

public class OPCodeInfo {
    private static final OPCodeInfo[] INFO = new OPCodeInfo[0xff];

    static {
        INFO[nop] = info("nop");

        // Бинарные и унарные операции.
        INFO[add] = info("add", Param.REG, Param.REG, Param.REG);
        INFO[sub] = info("sub", Param.REG, Param.REG, Param.REG);
        INFO[mul] = info("mul", Param.REG, Param.REG, Param.REG);
        INFO[div] = info("div", Param.REG, Param.REG, Param.REG);
        INFO[rem] = info("rem", Param.REG, Param.REG, Param.REG);
        INFO[bit_and] = info("bit-and", Param.REG, Param.REG, Param.REG);
        INFO[bit_or] = info("bit-or", Param.REG, Param.REG, Param.REG);
        INFO[bit_xor] = info("bit-xor", Param.REG, Param.REG, Param.REG);
        INFO[bit_inv] = info("bit-inv", Param.REG, Param.REG, Param.REG);
        INFO[neg] = info("neg", Param.REG, Param.REG);

        INFO[const_m1] = info("const_m1", Param.REG);
        INFO[const_0] = info("const_0", Param.REG);
        INFO[const_1] = info("const_1", Param.REG);
        INFO[const_2] = info("const_2", Param.REG);

        // Загружает значение из пула констант.
        INFO[load] = info("load", Param.CONST_ID, Param.REG);

        // Зануляет регистр/
        INFO[reset] = info("reset", Param.REG);

        // Копирует значение из левого регистра в правый.
        INFO[mov] = info("mov", Param.REG, Param.REG);

        // Сравнивают два значения и переключают флаг.
        INFO[cmp_eq] = info("cmp-eq", Param.REG, Param.REG);
        INFO[cmp_ne] = info("cmp-ne", Param.REG, Param.REG);
        INFO[cmp_lt] = info("cmp-lt", Param.REG, Param.REG);
        INFO[cmp_ge] = info("cmp-ge", Param.REG, Param.REG);
        INFO[cmp_gt] = info("cmp-gt", Param.REG, Param.REG);
        INFO[cmp_le] = info("cmp-le", Param.REG, Param.REG);

        // Безусловный прыжок.
        INFO[jump] = info("jump", Param.BCI);

        // Условный прыжок, если флаг активен.
        INFO[jmp_z] = info("jmp-z", Param.BCI);

        // Условный прыжок, если флаг неактивен.
        INFO[jmp_nz] = info("jmp-nz", Param.BCI);

        INFO[_return] = info("ret", Param.REG);
        INFO[leave] = info("leave");
    }

    private static OPCodeInfo info(String name, Param... params) {
        return new OPCodeInfo(name, params);
    }

    @Deprecated
    public static String opcodeString(int opcode) {
        return info(opcode).name;
    }

    @Deprecated
    public static int weight(int opcode) {
        return info(opcode).getWeight();
    }

    public static OPCodeInfo info(int opcode) {
        if (0 > opcode || opcode >= 0xff || INFO[opcode] == null) {
            throw new IllegalArgumentException("Illegal opcode: " + opcode);
        }
        return INFO[opcode];
    }

    public final String name;
    public final Param[] params;

    private OPCodeInfo(String name, Param[] params) {
        this.name = name;
        this.params = params;
    }

    public int getWeight() {
        int w = 1; // Opcode byte

        for (Param param : params) {
            switch (param) {
                case REG:
                    w += 1;
                    break;
                case BCI:
                    w += 2;
                    break;
            }
        }

        return w;
    }

    public enum Param {
        REG, // 1 byte
        CONST_ID, // 2 bytes
        BCI // 2 bytes
    }
}
