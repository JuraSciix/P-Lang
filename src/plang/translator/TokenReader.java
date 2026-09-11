package plang.translator;

import java.nio.CharBuffer;
import java.util.List;

public final class TokenReader {
    private final CharBuffer content;
    private final List<Token> tokens;
    private int pos = 0;

    public TokenReader(CharBuffer content, List<Token> tokens) {
        this.content = content;
        this.tokens = tokens;
    }

    public boolean hasRemaining() {
        return pos < tokens.size();
    }

    public Token currentToken() {
        return tokens.get(pos);
    }

    public Token getAndStep() {
        return tokens.get(pos++);
    }

    public void step() {
        pos++;
    }

    public void stepBack() {
        pos--;
    }

    public CharSequence getTokenData(Token token) {
        return content.subSequence(token.pos, token.endPos);
    }
}
