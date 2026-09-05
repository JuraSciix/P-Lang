package plang.translator.codegen;

import java.util.ArrayList;
import java.util.List;

public final class ConstTable {
    private static class Entry {
        final int value;
        final int index;

        Entry(int value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private int counter = 0;

    public int lookup(int value) {
        for (Entry entry : entries) {
            if (value == entry.value) {
                return entry.index;
            }
        }

        int index = counter++;
        entries.add(new Entry(value, index));
        return index;
    }

    public int[] getPoolArray() {
        int[] poolArray = new int[entries.size()];
        for (int i = 0; i < poolArray.length; i++) {
            poolArray[i] = entries.get(i).value;
        }
        return poolArray;
    }
}
