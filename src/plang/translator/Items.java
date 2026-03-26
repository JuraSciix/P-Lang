package plang.translator;

import static plang.interpreter.OPCodeList.*;

class Items {
    static final int ANY = -1;

    private final Code code;

    Items(Code code) {
        this.code = code;
    }

    OpItem operItem() {
        return new OpItem();
    }

    DynamicItem dynamicItem() {
        return new DynamicItem();
    }

    ValueItem valueItem(int index) {
        return new ValueItem(index);
    }

    LocalItem localItem(int index) {
        return new LocalItem(index);
    }

    ConstItem constItem(int constIndex) {
        return new ConstItem(constIndex);
    }

    abstract class Item {
        ValueItem load() {
            throw new UnsupportedOperationException();
        }

        ValueItem load(int index) {
            throw new UnsupportedOperationException();
        }

        void dispose() {
            // nop
        }

        ValueItem acceptLeft(Item item) {
            int index = item.load().index;
            return load(index);
        }

        ValueItem acceptRight(Item item, Item destination) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        CondItem toCond() {
            return new CondItem();
        }
    }

    class OpItem extends Item {
        // EMTPY
    }

    class DynamicItem extends Item {

        ValueItem load() {
            return load(code.allocReg());
        }

        ValueItem load(int index) {
            return localItem(index);
        }
    }

    class ValueItem extends Item {
        final int index;

        ValueItem(int index) {
            this.index = index;
        }

        @Override
        ValueItem load() {
            return this;
        }

        @Override
        ValueItem load(int index) {
            if (this.index == index) return this;
            code.emit2(mov, this.index, index);
            return valueItem(index);
        }

        @Override
        void dispose() {
            code.releaseReg(index);
        }

        @Override
        ValueItem acceptRight(Item item, Item destination) {
            return item.acceptLeft(dynamicItem());
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ValueItem)) return false;
            ValueItem other = (ValueItem) obj;
            return index == other.index;
        }
    }

    class LocalItem extends ValueItem {
        final int localIndex;

        LocalItem(int localIndex) {
            super(localIndex);
            this.localIndex = localIndex;
        }

        @Override
        void dispose() {
            // nope
        }

        @Override
        ValueItem acceptLeft(Item item) {
            return this;
        }

        @Override
        ValueItem acceptRight(Item item, Item destination) {
            if (equals(destination)) {
                // Мы не можем позволить перезаписать себя
                return super.acceptRight(item, destination);
            } else {
                // Дизпоуз item.acceptLeft() возможен только через дизпоуз destination. Не наоборот.
                int index = item.acceptLeft(destination).index;
                return localItem(index);
            }
        }
    }

    class ConstItem extends Item {
        final int constIndex;

        ConstItem(int constIndex) {
            this.constIndex = constIndex;
        }

        @Override
        ValueItem load() {
            return load(code.allocReg());
        }

        @Override
        ValueItem load(int index) {
            code.emit2(load, constIndex, index);
            return valueItem(index);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ConstItem)) return false;
            ConstItem other = (ConstItem) obj;
            return constIndex == other.constIndex;
        }
    }

    class CondItem extends Item {
        Code.Flow positiveCase;
        Code.Flow negativeCase;

        @Override
        ValueItem load() {
            int index = code.allocReg();
            code.emitUnary(cmp_eq, 0, index);
            return valueItem(index);
        }

        @Override
        CondItem toCond() {
            return this;
        }
    }
}
