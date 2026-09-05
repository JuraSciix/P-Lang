package plang.translator.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public final class LocalTable {
    private static class Entry {
        final String name;
        final int index;

        Entry(String name, int index) {
            this.name = name;
            this.index = index;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void register(String name, int index) {
        for (Entry entry : entries) {
            if (name.equals(entry.name)) {
                throw new IllegalArgumentException(name);
            }
        }
        entries.add(new Entry(name, index));
    }

    public int resolve(String name) {
        for (Entry entry : entries) {
            if (name.equals(entry.name)) {
                return entry.index;
            }
        }
        throw new NoSuchElementException(name);
    }

    public boolean contains(String name) {
        for (Entry entry : entries) {
            if (name.equals(entry.name)) {
                return true;
            }
        }
        return false;
    }
}
