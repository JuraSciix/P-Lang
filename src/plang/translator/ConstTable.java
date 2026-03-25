package plang.translator;

import java.util.ArrayList;
import java.util.List;

public final class ConstTable {
    private static class Entry {
        final Object value;
        final int index;

        Entry(Object value, int index) {
            this.value = value;
            this.index = index;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private int counter = 0;

    public int lookup(Object value) {
        for (Entry entry : entries) {
            if (value.equals(entry.value)) {
                return entry.index;
            }
        }

        int index = counter++;
        entries.add(new Entry(value, index));
        return index;
    }
}
