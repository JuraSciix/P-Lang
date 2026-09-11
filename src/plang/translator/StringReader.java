package plang.translator;

import java.nio.CharBuffer;

public final class StringReader {
    private final CharBuffer content;
    private int pos = 0;

    public StringReader(CharBuffer content) {
        this.content = content;
    }

    public boolean hasRemaining() {
        return pos < content.remaining();
    }

    public int getPosition() {
        return pos;
    }

    public char currentChar() {
        return content.get(pos);
    }

    public void step() {
        pos++;
    }

    public boolean matches(char ch) {
        if (hasRemaining() && currentChar() == ch) {
            step();
            return true;
        }
        return false;
    }
}
