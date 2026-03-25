package plang.translator;

import plang.interpreter.OPCodeList;
import plang.translator.Ast.Expr.Tag;

public class AstInfo {

    public static int opcodeFromTag(Tag tag) {
        if (0 <= tag.compareTo(Tag.ADD) && tag.compareTo(Tag.REM) <= 0) {
            // tag iz [ADD..REM]
            return tag.ordinal() - Tag.ADD.ordinal() + OPCodeList.add;
        }

        if (tag == Tag.NEG) {
            return OPCodeList.neg;
        }

        throw new IllegalArgumentException();
    }
}
