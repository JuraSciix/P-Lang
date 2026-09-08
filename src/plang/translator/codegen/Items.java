package plang.translator.codegen;

import static plang.interpreter.OPCodeList.*;

public class Items {
    private final Code code;
    private final CodeEmitter emitter;
    private final ConstTable constTable;

    Items(Code code, CodeEmitter emitter, ConstTable constTable) {
        this.code = code;
        this.emitter = emitter;
        this.constTable = constTable;
    }

    DirectDest direct() {
        return new DirectDest();
    }

    StableDest stable(int index) {
        return new StableDest(index);
    }

    CondItem cond(Dest dest) {
        return new CondItem(jmp_z, dest);
    }

    GraphItem graph() {
        return new GraphItem();
    }

    /**
     * Слот для записи.
     */
    public abstract class Dest {
        /**
         * Подготавливает слот к использованию. Может создаваться новый одноразовый слот.
         */
        Item prepare() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Присваивает в слот значение из пула констант.
         *
         * @param value Значение константы.
         * @return Слот со значением константы.
         */
        Item storeConst(long value) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Присваивает в слот значение из переменной.
         *
         * @param stableIndex Индекс переменной.
         * @return Слот со значением переменной.
         */
        Item storeStable(int stableIndex) {
            throw new UnsupportedOperationException(getClass().getName());
        }
    }

    /**
     * Прямой слот. Используется для того, чтобы напрямую получать значения.
     */
    class DirectDest extends Dest {
        @Override
        Item prepare() {
            return new OneTimeItem();
        }

        @Override
        Item storeStable(int stableIndex) {
            return new StableItem(stableIndex);
        }

        @Override
        Item storeConst(long value) {
            OneTimeItem item = new OneTimeItem();
            if (-1L <= value && value <= 2L) {
                emitter.emitBB(const_0 + (int) value, item.index());
            } else {
                emitter.emitBSB(load, constTable.lookup(value), item.index());
            }
            return item;
        }
    }

    /**
     * Прямой слот. Используется для того, чтобы напрямую получать значения.
     */
    class StableDest extends Dest {
        final int index;

        StableDest(int index) {
            this.index = index;
        }

        @Override
        Item prepare() {
            return new StableItem(index);
        }

        @Override
        Item storeStable(int stableIndex) {
            emitter.emitBBB(mov, stableIndex, index);
            return new StableItem(stableIndex);
        }

        @Override
        Item storeConst(long value) {
            if (-1L <= value && value <= 2L) {
                emitter.emitBB(const_0 + (int) value, index);
            } else {
                emitter.emitBSB(load, constTable.lookup(value), index);
            }
            return prepare();
        }
    }

    /**
     * Слот - это функциональная единица для манипуляции регистрами.
     */
    public abstract class Item {

        /**
         * Превращает слот в логический.
         */
        CondItem toCond(Dest dest) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Объявляет регистр использованным.
         * После этого регистр может автоматически освободиться.
         */
        Item use() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Возвращает индекс слота.
         */
        public int index() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        public boolean alive() {
            return true;
        }
    }

    /**
     * Пустой предмет. Ничего не удерживает, ничего не освобождает.
     */
    class GraphItem extends Item {
        private boolean alive = true;

        GraphItem aliveness(boolean state) {
            alive = state;
            return this;
        }

        @Override
        Item use() {
            return this;
        }

        @Override
        public boolean alive() {
            return alive;
        }
    }

    /**
     * Стабильный слот. Это переменная. Индекс не освобождается.
     */
    class StableItem extends Item {
        private final int index;

        StableItem(int index) {
            this.index = index;
        }

        @Override
        CondItem toCond(Dest dest) {
            int unitIndex = code.acquire();
            emitter.emitBB(const_0, unitIndex);
            emitter.emitBBB(cmp_ne, index(), unitIndex);
            code.release(unitIndex);
            return new CondItem(jmp_z, dest);
        }

        @Override
        Item use() {
            return this;
        }

        @Override
        public int index() { return index; }
    }

    /**
     * Одноразовый слот.
     * Служит для разовых операций, таких как сложение.
     * Индекс тоже одноразовый и становится недействительным после использования.
     */
    class OneTimeItem extends StableItem {
        OneTimeItem() {
            super(code.acquire());
        }

        @Override
        Item use() {
            code.release(index());
            return this;
        }
    }

    /**
     * Слот над логическим значением.
     */
    class CondItem extends Item {
        private int opcode;
        private Dest dest;
        private Mark trueMarks;
        private Mark falseMarks;

        CondItem(int opcode, Dest dest) {
            this.opcode = opcode;
            this.dest = dest;
        }

        CondItem emitFalseJump() {
            emitter.emitBS(opcode, 0);
            falseMarks = emitter.mark(falseMarks);
            emitter.close(trueMarks);
            return this;
        }

        CondItem emitTrueJump() {
            emitter.emitBS(OPCodes.negate(opcode), 0);
            trueMarks = emitter.mark(trueMarks);
            emitter.close(falseMarks);
            return this;
        }

        CondItem negate() {
            opcode = OPCodes.negate(opcode);
            Mark tmp = falseMarks;
            falseMarks = trueMarks;
            trueMarks = tmp;
            return this;
        }

        void closeFalseJumps() {
            emitter.close(falseMarks);
            falseMarks = null;
        }

        CondItem coalesceTrueJumps(CondItem item) {
            trueMarks = Mark.merge(trueMarks, item.trueMarks);
            return this;
        }

        CondItem coalesceFalseJumps(CondItem item) {
            falseMarks = Mark.merge(falseMarks, item.falseMarks);
            return this;
        }

        @Override
        CondItem toCond(Dest dest) {
            this.dest = dest;
            return this;
        }

        @Override
        Item use() {
            Item item = dest.prepare();
            emitter.emitBS(opcode, 0);
            Mark elseMark = emitter.mark(falseMarks);
            emitter.close(trueMarks);
            emitter.emitBB(const_1, item.index());
            Mark exitMark = emitter.mark(jump, null);
            emitter.close(elseMark);
            emitter.emitBB(const_0, item.index());
            emitter.close(exitMark);
            return item;
        }
    }
}
