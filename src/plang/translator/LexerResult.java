package plang.translator;

import java.util.Collections;
import java.util.List;

public final class LexerResult {

    private final String sourceName;
    private final LineNumberMap lineNumberMap;
    private final List<Token> tokens;
    private final List<TokenData> data;

    public LexerResult(String sourceName, LineNumberMap lineNumberMap, List<Token> tokens, List<TokenData> data) {
        this.sourceName = sourceName;
        this.lineNumberMap = lineNumberMap;
        this.tokens = tokens;
        this.data = data;
    }

    public String getSourceName() {
        return sourceName;
    }

    public LineNumberMap getLineNumberMap() {
        return lineNumberMap;
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public List<TokenData> getData() {
        return Collections.unmodifiableList(data);
    }
}
