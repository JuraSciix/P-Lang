package plang.translator.codegen;

import plang.utils.LongArrayList;

public final class ConstTable {
    private final LongArrayList entries = new LongArrayList();

    public LongArrayList entries() {
        return entries;
    }

    public int lookup(long value) {
        int i;
        if ((i = entries.indexOf(value)) >= 0)
            return i;
        entries.add(value);
        return entries.lastIndexOf(value);
    }
}
