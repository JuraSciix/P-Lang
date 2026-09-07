package plang.translator.codegen;

import java.util.ArrayList;
import java.util.List;

public final class ConstTable {
    private static class Entry {
        final long value;
        final int index;

        Entry(long value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private int counter = 0;

    public int lookup(long value) {
        for (Entry entry : entries) {
            if (value == entry.value) {
                return entry.index;
            }
        }

        int index = counter++;
        entries.add(new Entry(value, index));
        return index;
    }

    public long[] getPoolArray() {
        long[] poolArray = new long[entries.size()];
        for (int i = 0; i < poolArray.length; i++) {
            poolArray[i] = entries.get(i).value;
        }
        return poolArray;
    }
}
