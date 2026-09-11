package plang.translator;

import plang.translator.Ast.Expr.Tag;

public class ParseHelper {

    public static boolean isTokenTypeOfBinary(TokenType type) {
        return 0 <= type.compareTo(TokenType.PLUS) && type.compareTo(TokenType.BARBAR) <= 0;
    }

    public static long parseLong(CharSequence csq) {
        long result = 0L;
        for (int i = 0; i < csq.length(); i++) {
            char c = csq.charAt(i);
            if (c != '_') {
                result = result * 10L + (c - '0');
            }
        }
        return result;
    }

    public static Tag tagOf(TokenType type) {
        switch (type) {
            case PLUS: return Tag.ADD;
            case MINUS: return Tag.SUB;
            case STAR: return Tag.MUL;
            case SLASH: return Tag.DIV;
            case PERCENT: return Tag.REM;
            case EQ: return Tag.CMP_EQ;
            case NOT_EQ: return Tag.CMP_NE;
            case LT: return Tag.CMP_LT;
            case LT_EQ: return Tag.CMP_LE;
            case GT: return Tag.CMP_GT;
            case GT_EQ: return Tag.CMP_GE;
            case AMP: return Tag.BIT_AND;
            case BAR: return Tag.BIT_OR;
            case AMPAMP: return Tag.CON;
            case BARBAR: return Tag.DIS;
            default: throw new AssertionError(type);
        }
    }

    /**
     * Возвращает приоритет бинарной операции:
     * чем больше значение - тем ниже приоритет.
     * Чем ниже приоритет - тем позднее вычисляется операция.
     */
    public static int precedence(Tag tag) {
        switch (tag) {
            case MUL: case DIV: case REM:
                return 100;
            case ADD: case SUB:
                return 200;
            case BIT_AND:
                return 300;
            case BIT_OR:
                return 400;
            case BIT_XOR:
                return 500;
            case CMP_LT: case CMP_GT:
            case CMP_LE: case CMP_GE:
                return 600;
            case CMP_EQ: case CMP_NE:
                return 700;
            case CON:
                return 800;
            case DIS:
                return 900;
            default:
                throw new AssertionError(tag);
        }
    }

    public static int higher(int precedence) {
        return precedence - 100;
    }
}
