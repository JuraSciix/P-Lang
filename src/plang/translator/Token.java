package plang.translator;

public final class Token {
    public final int pos;
    public final TokenType type;

    public Token(int pos, TokenType type) {
        this.pos = pos;
        this.type = type;
    }

    public boolean hasType(TokenType type) {
        return this.type == type;
    }

    @Override
    public String toString() {
        return Token.class.getName() + "(pos=" + pos + ", type=" + type + ")";
    }
}
