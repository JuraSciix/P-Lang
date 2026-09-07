package plang.translator.codegen;

/**
 * Цепочка маркировок в коде.
 */
public final class Mark {
    public static Mark merge(Mark x, Mark y) {
        // Рекурсивная перестройка цепочки.
        if (x == null) return y;
        if (y == null) return x;
        return new Mark(x.index, merge(x.prev, y));
    }

    public final int index;
    public final Mark prev;

    public Mark(int index, Mark prev) {
        this.index = index;
        this.prev = prev;
    }
}
