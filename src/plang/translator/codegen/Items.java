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

    CondItem cond(int opcode) {
        return new CondItem(opcode);
    }

    /**
     * Слот - это функциональная единица для манипуляции регистрами.
     */
    abstract static class Item {
        /**
         * Подготавливает слот к использованию. Может создаваться новый одноразовый слот.
         */
        Item prepare() {
            throw new UnsupportedOperationException(getClass().getName());
        }

        /**
         * Превращает слот в логический.
         */
        CondItem cond() {
            use();
            return new CondItem(jmp_z);
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
            return prepare().storeConst(constIndex);
        }

        /**
         * Присваивает в слот значение из переменной.
         *
         * @param stableIndex Индекс переменной.
         * @return Слот со значением переменной.
         */
        Item storeStable(int stableIndex) {
            return prepare().storeStable(stableIndex);
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
    static class CondItem extends Item {
        final int opcode;

        CondItem(int opcode) {
            this.opcode = opcode;
        }

        CondItem negate() {
            return new CondItem(OPCodes.negate(opcode));
        }

        @Override
        CondItem cond() {
            return this;
        }

        @Override
        int use() {
            return 0; // todo
        }
    }
}
