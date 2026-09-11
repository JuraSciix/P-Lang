package plang.translator.codegen;

public final class OPCodes {
    private OPCodes() {
        throw new AssertionError();
    }

    /**
     * Инверсирует операции {@code cmp-*} и {@code jump-*}.
     *
     * @param opcode Код инструкции.
     * @return Инверсированный код инструкции
     */
    public static int negate(int opcode) {
        return ((opcode - 1) ^ 1) + 1;
    }
}
