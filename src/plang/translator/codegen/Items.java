package plang.translator.codegen;

import static plang.interpreter.OPCodeList.load;
import static plang.interpreter.OPCodeList.mov;

class Items {
    private final Code code;
    private final ConstTable constTable;

    Items(Code code, ConstTable constTable) {
        this.code = code;
        this.constTable = constTable;
    }

    DynamicItem dynamic() {
        return new DynamicItem();
    }

    StableItem stable(int index) {
        return new StableItem(index);
    }

    DirectItem direct() {
        return new DirectItem();
    }

    CondItem cond() {
        return new CondItem();
    }

    abstract static class Item {
        Item prepare() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        int get() {
            throw new UnsupportedOperationException();
        }

        CondItem cond() {
            throw new UnsupportedOperationException();
        }

        Item storeConstant(int index) {
            return prepare().storeConstant(index);
        }

        Item storeStable(int index) {
            return prepare().storeStable(index);
        }
    }

    class DynamicItem extends Item {
        @Override
        Item prepare() {
            return new OneTimeItem();
        }
    }

    class StableItem extends Item {
        final int index;

        protected StableItem(int index) {
            this.index = index;
        }

        @Override
        Item prepare() {
            return this;
        }

        @Override
        int get() {
            return index;
        }

        @Override
        Item storeStable(int index) {
            code.emit2(mov, index, this.index);
            return this;
        }

        @Override
        Item storeConstant(int index) {
            code.emit2UBWithUB(load, index, this.index);
            return this;
        }
    }

    class OneTimeItem extends StableItem {
        OneTimeItem() {
            super(code.allocReg());
        }

        @Override
        int get() {
            code.releaseReg(index);
            return super.get();
        }
    }

    class DirectItem extends DynamicItem {

        @Override
        Item storeStable(int index) {
            return new StableItem(index);
        }

        @Override
        Item storeConstant(int index) {
            OneTimeItem item = new OneTimeItem();
            code.emit2UBWithUB(load, index, item.index);
            return item;
        }
    }

    static class CondItem extends Item {

        @Override
        CondItem cond() {
            return this;
        }

        @Override
        int get() {
            return 0; // todo
        }
    }
}
