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

        Item accept(Item item) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        CondItem toCond() {
            return new CondItem();
        }
    }

    class OpItem extends Item {
    }

    class DynamicItem extends Item {

        ValueItem load() {
            return load(code.allocReg());
        }

        ValueItem load(int index) {
            return localItem(index);
        }

        @Override
        Item accept(Item item) {
            return item;
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
        Item accept(Item item) {
            return item.load(index);
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
        Item accept(Item item) {
            return this;
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
