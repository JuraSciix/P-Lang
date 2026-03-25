package plang.translator;

public final class StringReader {
    private final char[] data;

    private int pos = 0;

    public StringReader(String str) {
        data = str.toCharArray();
    }

    public boolean hasRemaining() {
        return pos < data.length;
    }

    public int getPosition() {
        return pos;
    }

    public char currentChar() {
        return data[pos];
    }

    public void step() {
        pos++;
    }

    public void stepBack() {
        pos--;
    }
}
