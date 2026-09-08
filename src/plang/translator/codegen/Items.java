package plang.translator.codegen;

import static plang.interpreter.OPCodeList.*;

class Items {
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

    StableDest stableDest(int index) {
        return new StableDest(index);
    }

    EmptyItem empty() {
        return new EmptyItem();
    }

    StableItem stable(int index) {
        return new StableItem(index);
    }

    CondItem cond(Dest dest) {
        return new CondItem(jmp_z, dest);
    }

    /**
     * Слот для записи.
     */
    abstract class Dest {
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
                emitter.emitBB(const_0 + (int) value, item.index);
            } else {
                emitter.emitBSB(load, constTable.lookup(value), item.index);
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
    abstract class Item {

        /**
         * Превращает слот в логический.
         */
        CondItem cond(Dest dest) {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Возвращает индекс слота.
         */
        int index() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Объявляет регистр использованным.
         * После этого регистр может автоматически освободиться.
         */
        int use() {
            return index();
        }
    }

    /**
     * Пустой предмет. Ничего не удерживает, ничего не освобождает.
     */
    class EmptyItem extends Item {
        @Override
        int use() {
            return -1;
        }
    }

    /**
     * Стабильный слот. Это переменная. Индекс не освобождается.
     */
    class StableItem extends Item {
        final int index;

        StableItem(int index) {
            this.index = index;
        }

        @Override
        CondItem cond(Dest dest) {
            int unitIndex = code.acquire();
            int itemIndex = use();
            emitter.emitBB(const_0, unitIndex);
            emitter.emitBBB(cmp_ne, itemIndex, unitIndex);
            code.release(unitIndex);
            return new CondItem(jmp_z, dest);
        }

        @Override
        int index() {
            return index;
        }
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
        int use() {
            code.release(index);
            return super.use();
        }
    }

    /**
     * Слот над логическим значением.
     */
    class CondItem extends Item {
        int opcode;
        Dest dest;
        Mark trueMarks;
        Mark falseMarks;

        CondItem(int opcode, Dest dest) {
            this.opcode = opcode;
            this.dest = dest;
        }

        CondItem negate() {
            opcode = OPCodes.negate(opcode);
            Mark tmp = falseMarks;
            falseMarks = trueMarks;
            trueMarks = tmp;
            return this;
        }

        void emitFalseJump() {
            emitter.emitBS(opcode, 0);
            falseMarks = emitter.mark(falseMarks);
        }

        void emitTrueJump() {
            emitter.emitBS(OPCodes.negate(opcode), 0);
            trueMarks = emitter.mark(trueMarks);
        }

        void closeTrue() {
            emitter.close(trueMarks);
            trueMarks = null;
        }

        void closeFalse() {
            emitter.close(falseMarks);
            falseMarks = null;
        }

        CondItem inherit(int opcode, Mark trueMarks, Mark falseMarks) {
            this.opcode = opcode;
            this.trueMarks = trueMarks;
            this.falseMarks = falseMarks;
            return this;
        }

        @Override
        CondItem cond(Dest dest) {
            this.dest = dest;
            return this;
        }

        @Override
        int use() {
            int destIndex = dest.prepare().use();
            emitFalseJump();
            closeTrue();
            emitter.emitBB(const_1, destIndex);
            Mark mark = emitter.mark(jump, null);
            closeFalse();
            emitter.emitBB(const_0, destIndex);
            emitter.close(mark);
            return destIndex;
        }
    }
}
