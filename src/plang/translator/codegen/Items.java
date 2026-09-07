package plang.translator.codegen;

import static plang.interpreter.OPCodeList.*;

class Items {
    private final Code code;
    private final CodeEmitter emitter;

    Items(Code code, CodeEmitter emitter) {
        this.code = code;
        this.emitter = emitter;
    }

    DirectItem direct() {
        return new DirectItem();
    }

    StableItem stable(int index) {
        return new StableItem(index);
    }

    CondItem cond(Item dest) {
        return new CondItem(jmp_z, dest);
    }

    /**
     * Слот - это функциональная единица для манипуляции регистрами.
     */
    abstract class Item {
        /**
         * Подготавливает слот к использованию. Может создаваться новый одноразовый слот.
         */
        Item prepare() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Превращает слот в логический.
         */
        CondItem cond(Item dest) {
            use();
            return new CondItem(jmp_z, dest);
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

        /**
         * Присваивает в слот значение из пула констант.
         *
         * @param constIndex Индекс константы в пуле.
         * @return Слот со значением константы.
         */
        Item storeConst(int constIndex) {
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
    class DirectItem extends Item {
        @Override
        Item prepare() {
            return new OneTimeItem();
        }

        @Override
        Item storeStable(int stableIndex) {
            return new StableItem(stableIndex);
        }

        @Override
        Item storeConst(int constIndex) {
            OneTimeItem item = new OneTimeItem();
            emitter.emitBSB(load, constIndex, item.index);
            return item;
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
        Item prepare() {
            return this;
        }

        @Override
        int index() {
            return index;
        }

        @Override
        Item storeStable(int stableIndex) {
            emitter.emitBBB(mov, stableIndex, index);
            return this;
        }

        @Override
        Item storeConst(int constIndex) {
            emitter.emitBSB(load, constIndex, index);
            return this;
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
        final int opcode;
        final Item dest;

        CondItem(int opcode, Item dest) {
            this.opcode = opcode;
            this.dest = dest;
        }

        CondItem negate() {
            return new CondItem(OPCodes.negate(opcode), dest);
        }

        @Override
        CondItem cond(Item dest) {
            return dest != null ? new CondItem(opcode, dest) : this;
        }

        @Override
        int use() {
            int index = dest.prepare().use();
            emitter.emitBS(opcode, 0);
            Mark m0 = emitter.mark(null);
            emitter.emitBB(const_1, index);
            emitter.emitBS(jump, 0);
            Mark m1 = emitter.mark(null);
            emitter.close(m0);
            emitter.emitBB(const_0, index);
            emitter.close(m1);
            return index;
        }
    }
}
