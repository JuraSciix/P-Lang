package plang.interpreter;

import static plang.interpreter.OPCodeList.*;

public final class OPCodeInfo {
    private static final OPCodeInfo[] INFO = new OPCodeInfo[0xff];

    static {
        INFO[nop] = make("nop");

        // Бинарные и унарные операции.
        INFO[add] = make("add", Param.REG, Param.REG, Param.REG);
        INFO[sub] = make("sub", Param.REG, Param.REG, Param.REG);
        INFO[mul] = make("mul", Param.REG, Param.REG, Param.REG);
        INFO[div] = make("div", Param.REG, Param.REG, Param.REG);
        INFO[rem] = make("rem", Param.REG, Param.REG, Param.REG);
        INFO[bit_and] = make("bit-and", Param.REG, Param.REG, Param.REG);
        INFO[bit_or] = make("bit-or", Param.REG, Param.REG, Param.REG);
        INFO[bit_xor] = make("bit-xor", Param.REG, Param.REG, Param.REG);
        INFO[bit_inv] = make("bit-inv", Param.REG, Param.REG, Param.REG);
        INFO[neg] = make("neg", Param.REG, Param.REG);

        INFO[const_m1] = make("const_m1", Param.REG);
        INFO[const_0] = make("const_0", Param.REG);
        INFO[const_1] = make("const_1", Param.REG);
        INFO[const_2] = make("const_2", Param.REG);

        // Загружает значение из пула констант.
        INFO[load] = make("load", Param.CONST_ID, Param.REG);

        // Зануляет регистр/
        INFO[reset] = make("reset", Param.REG);

        // Копирует значение из левого регистра в правый.
        INFO[mov] = make("mov", Param.REG, Param.REG);

        // Сравнивают два значения и переключают флаг.
        INFO[cmp_eq] = make("cmp-eq", Param.REG, Param.REG);
        INFO[cmp_ne] = make("cmp-ne", Param.REG, Param.REG);
        INFO[cmp_lt] = make("cmp-lt", Param.REG, Param.REG);
        INFO[cmp_ge] = make("cmp-ge", Param.REG, Param.REG);
        INFO[cmp_gt] = make("cmp-gt", Param.REG, Param.REG);
        INFO[cmp_le] = make("cmp-le", Param.REG, Param.REG);

        // Безусловный прыжок.
        INFO[jump] = make("jump", Param.BCI);

        // Условный прыжок, если флаг активен.
        INFO[jmp_z] = make("jmp-z", Param.BCI);

        // Условный прыжок, если флаг неактивен.
        INFO[jmp_nz] = make("jmp-nz", Param.BCI);

        INFO[_return] = make("ret", Param.REG);
        INFO[leave] = make("leave");
    }

    private static OPCodeInfo make(String name, Param... params) {
        return new OPCodeInfo(name, params);
    }

    public static OPCodeInfo of(int opcode) {
        if (0 > opcode || opcode >= 0xff || INFO[opcode] == null) {
            throw new IllegalArgumentException("Illegal opcode: " + opcode);
        }
        return INFO[opcode];
    }

    private final String name;
    private final Param[] params;

    private OPCodeInfo(String name, Param[] params) {
        this.name = name;
        this.params = params;
    }

    public String name() {
        return name;
    }

    public Param[] params() {
        return params.clone();
    }

    public enum Param {
        REG, // 1 byte
        CONST_ID, // 2 bytes
        BCI // 2 bytes
    }
}
