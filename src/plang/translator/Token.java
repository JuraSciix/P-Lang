package plang.translator;

public final class Token {
    public final int pos, endPos;
    public final TokenType type;

    public Token(int pos, int endPos, TokenType type) {
        this.pos = pos;
        this.endPos = endPos;
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
