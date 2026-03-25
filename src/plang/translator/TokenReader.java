package plang.translator;

import java.util.List;
import java.util.NoSuchElementException;

public final class TokenReader {
    private final List<Token> tokens;
    private final List<TokenData> data;
    private int pos = 0;

    public TokenReader(List<Token> tokens, List<TokenData> data) {
        this.tokens = tokens;
        this.data = data;
    }

    public boolean hasRemaining() {
        return pos < tokens.size();
    }

    public Token currentToken() {
        return tokens.get(pos);
    }

    public void step() {
        pos++;
    }

    public void stepBack() {
        pos--;
    }

    public String getTokenData(Token token) {
        // Заметка: можно использовать бинарный поиск,
        // но простоты ради будет линейный.
        for (TokenData td : data) {
            if (token.pos == td.pos) {
                return td.data;
            }
        }

        throw new NoSuchElementException();
    }
}
