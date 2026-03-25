package plang.translator;

public final class LineNumberMap {

    private final int[] lineStartPositions;

    public LineNumberMap(int[] lineStartPositions) {
        this.lineStartPositions = lineStartPositions;
    }

    public int getLineNumber(int pos) {
        int i = 0;
        while (i < lineStartPositions.length) {
            if (pos < lineStartPositions[i]) {
                break;
            }
            i++;
        }

        return i + 1;
    }
}
