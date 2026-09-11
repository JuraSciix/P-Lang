package plang.translator;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.List;

public final class LexResult {

    private final String sourceName;
    private final CharBuffer content;
    private final LineNumberMap lineNumberMap;
    private final List<Token> tokens;

    public LexResult(String sourceName, CharBuffer content, LineNumberMap lineNumberMap, List<Token> tokens) {
        this.sourceName = sourceName;
        this.content = content;
        this.lineNumberMap = lineNumberMap;
        this.tokens = tokens;
    }

    public String getSourceName() {
        return sourceName;
    }

    public CharBuffer getContent() {
        return content;
    }

    public LineNumberMap getLineNumberMap() {
        return lineNumberMap;
    }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

}
