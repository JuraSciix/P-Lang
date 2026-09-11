package plang.utils;

public final class CharSubSequence implements CharSequence{
    private final CharSequence origin;
    private final int from, to;

    private boolean hashUncalculated = true;
    private int hash;

    public CharSubSequence(CharSequence origin, int from, int to) {
        this.origin = origin;
        this.from = from;
        this.to = to;
    }

    @Override
    public int length() {
        return to - from;
    }

    @Override
    public char charAt(int index) {
        return origin.charAt(from + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return origin.subSequence(from + start, from + end);
    }

    @Override
    public int hashCode() {
        if (hashUncalculated) {
            int h = 0;
            for (int i = 0; i < length(); i++)
                h = 31 * h + charAt(i);
            hash = h;
            hashUncalculated = false;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof CharSequence)) return false;
        CharSequence csq = (CharSequence) obj;
        if (csq.length() != length()) return false;
        for (int i = 0; i < length(); i++)
            if (csq.charAt(i) != charAt(i))
                return false;
        return true;
    }

    @Override
    public String toString() {
        return origin.subSequence(from, to).toString();
    }
}
