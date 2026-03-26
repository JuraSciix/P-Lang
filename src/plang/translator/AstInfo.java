package plang.translator;

import plang.interpreter.OPCodeList;
import plang.translator.Ast.Expr.Tag;

public class AstInfo {

    public static int opcodeFromTag(Tag tag) {
        if (0 <= tag.compareTo(Tag.ADD) && tag.compareTo(Tag.BIT_INV) <= 0) {
            // tag iz [ADD..BIT_INV]
            return tag.ordinal() - Tag.ADD.ordinal() + OPCodeList.add;
        }

        if (tag == Tag.NEG) {
            return OPCodeList.neg;
        }

        if (0 <= tag.compareTo(Tag.CMP_EQ) && tag.compareTo(Tag.CMP_LE) <= 0) {
            // tag iz [CMP_EQ..CMP_LE]
            return tag.ordinal() - Tag.CMP_EQ.ordinal() + OPCodeList.cmp_eq;
        }

        throw new AssertionError(tag.name());
    }

    public static boolean isComparing(Tag tag) {
        return 0 <= tag.compareTo(Tag.CMP_EQ) && tag.compareTo(Tag.CMP_LE) <= 0;
    }
}
