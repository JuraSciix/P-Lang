package plang.translator.codegen;

/**
 * Маркировка в коде.
 */
public final class Mark {
    public final int index;
    public final Mark prev;

    public Mark(int index, Mark prev) {
        this.index = index;
        this.prev = prev;
    }
}
