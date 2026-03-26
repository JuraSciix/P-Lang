package plang.translator;

import static plang.interpreter.OPCodeList.*;

class Items {
    private final Code code;
    private final ConstTable constTable;

    Items(Code code, ConstTable constTable) {
        this.code = code;
        this.constTable = constTable;
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

    CondItem condItem() {
        return new CondItem();
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
            ValueItem left = item.load();
            load(left.index);
            // Возвращаем left, чтобы сохранить свойство disposable.
            return left;
        }

        ValueItem acceptRight(Item item, Item destination) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        LocalItem local() {
            ValueItem loaded = load();
            return localItem(loaded.index);
        }

        CondItem cond() {
            ValueItem item = load();
            ValueItem zero = constItem(constTable.lookup(0)).load();
            code.emitUnary(cmp_ne, item.index, zero.index);
            item.dispose();
            zero.dispose();
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
            return valueItem(index);
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
            if (index == this.index) return this;
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
                return item.acceptLeft(destination);
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
            code.emit2UBWithUB(load, constIndex, index);
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
        final int opcode;
        Code.Jump falseJump = null;
        Code.Jump trueJump = null;

        CondItem() {
            this(jmp_z);
        }

        CondItem(int opcode) {
            this.opcode = opcode;
        }

        @Override
        ValueItem load() {
            return load(code.allocReg());
        }

        @Override
        ValueItem load(int index) {
            Item zero = constItem(constTable.lookup(0));
            Item one = constItem(constTable.lookup(1));

            resolveTrueJumps();
            one.load(index);
            Code.Jump leave = code.jump(jump);
            resolveFalseJumps();
            zero.load(index);
            code.resolveJump(leave);

            return valueItem(index);
        }

        @Override
        CondItem cond() {
            return this;
        }

        CondItem negate() {
            return new CondItem(OPCodes.negate(opcode));
        }

        void resolveTrueJumps() {
            falseJump = code.jump(opcode);
            if (trueJump != null) {
                code.resolveJump(trueJump);
            }
        }

        void resolveFalseJumps() {
            code.resolveJump(falseJump);
        }
    }
}
