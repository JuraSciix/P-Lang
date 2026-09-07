package plang.interpreter;

public final class Arithm {
    private Arithm() {
        throw new AssertionError();
    }

    public static boolean compare(long lhs, long rhs, boolean eq, boolean gt, boolean lt) {
        return eq && lhs == rhs || gt && lhs > rhs || lt && lhs < rhs;
    }
}
