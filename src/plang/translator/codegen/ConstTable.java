package plang.translator.codegen;

import plang.utils.LongArrayList;

public final class ConstTable {
    private final LongArrayList entries = new LongArrayList();

    public int lookup(long value) {
        int index = indexOf(value);
        if (index >= 0) {
            return index;
        } else {
            int nextIndex = entries.size();
            entries.add(value);
            return nextIndex;
        }
    }

    public int indexOf(long value) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) == value) {
                return i;
            }
        }
        return -1;
    }

    public long[] getPoolArray() {
        return entries.toArray();
    }
}
