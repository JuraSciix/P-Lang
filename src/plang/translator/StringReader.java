package plang.translator;

import java.nio.CharBuffer;

public final class StringReader {
    private final CharBuffer content;

    public StringReader(CharBuffer content) {
        this.content = content;
    }

    public boolean hasRemaining() {
        return content.hasRemaining();
    }

    public int getPosition() {
        return content.position();
    }

    public char currentChar() {
        return content.get(content.position());
    }

    public void step() {
        content.get();
    }

    public boolean matches(char ch) {
        if (hasRemaining() && currentChar() == ch) {
            step();
            return true;
        }
        return false;
    }
}
