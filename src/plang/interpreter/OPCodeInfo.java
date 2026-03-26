package plang.interpreter;

import static plang.interpreter.OPCodeList.*;

public class OPCodeInfo {
    private static final OPCodeInfo[] INFO = new OPCodeInfo[0xff];

    static {
        INFO[nop] = info("nop");

        // Бинарные и унарные операции.
        INFO[add] = info("add", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[sub] = info("sub", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[mul] = info("mul", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[div] = info("div", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[rem] = info("rem", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[bit_and] = info("bit-and", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[bit_or] = info("bit-or", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[bit_xor] = info("bit-xor", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[bit_inv] = info("bit-inv", Param.INDEX, Param.INDEX, Param.INDEX);
        INFO[neg] = info("neg", Param.INDEX, Param.INDEX);

        // Загружает значение из пула констант.
        INFO[load] = info("load", Param.INDEX, Param.INDEX);

        // Зануляет регистр/
        INFO[reset] = info("reset", Param.INDEX);

        // Копирует значение из левого регистра в правый.
        INFO[mov] = info("mov", Param.INDEX, Param.INDEX);

        // Сравнивают два значения и переключают флаг.
        INFO[cmp_eq] = info("cmp-eq", Param.INDEX, Param.INDEX);
        INFO[cmp_ne] = info("cmp-ne", Param.INDEX, Param.INDEX);
        INFO[cmp_lt] = info("cmp-lt", Param.INDEX, Param.INDEX);
        INFO[cmp_ge] = info("cmp-ge", Param.INDEX, Param.INDEX);
        INFO[cmp_gt] = info("cmp-gt", Param.INDEX, Param.INDEX);
        INFO[cmp_le] = info("cmp-le", Param.INDEX, Param.INDEX);

        // Безусловный прыжок.
        INFO[jump] = info("jump", Param.DOUBLE_INDEX);

        // Условный прыжок, если флаг активен.
        INFO[jmp_z] = info("jmp-z", Param.DOUBLE_INDEX);

        // Условный прыжок, если флаг неактивен.
        INFO[jmp_nz] = info("jmp-nz", Param.DOUBLE_INDEX);

        INFO[_return] = info("ret", Param.INDEX);
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
                case INDEX:
                    w += 1;
                    break;
                case DOUBLE_INDEX:
                    w += 2;
                    break;
            }
        }

        return w;
    }

    public enum Param {
        INDEX, // 1 byte
        DOUBLE_INDEX // 2 bytes
    }
}
