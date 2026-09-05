package plang.translator.codegen;

import static plang.interpreter.OPCodeList.load;
import static plang.interpreter.OPCodeList.mov;

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

    CondItem cond() {
        return new CondItem();
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

        /**
         * Присваивает в слот значение из пула констант.
         *
         * @param index Индекс константы в пуле.
         * @return Слот со значением константы.
         */
        Item storeConstant(int index) {
            return prepare().storeConstant(index);
        }

        /**
         * Присваивает в слот значение из переменной.
         *
         * @param index Индекс переменной.
         * @return Слот со значением переменной.
         */
        Item storeStable(int index) {
            return prepare().storeStable(index);
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
        Item storeStable(int index) {
            return new StableItem(index);
        }

        @Override
        Item storeConstant(int index) {
            OneTimeItem item = new OneTimeItem();
            emitter.emit2UBWithUB(load, index, item.index);
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
        Item storeStable(int index) {
            emitter.emit2(mov, index, this.index);
            return this;
        }

        @Override
        Item storeConstant(int index) {
            emitter.emit2UBWithUB(load, index, this.index);
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
            super(code.allocReg());
        }

        @Override
        int use() {
            code.releaseReg(index);
            return super.use();
        }
    }

    /**
     * Слот над логическим значением.
     */
    static class CondItem extends Item {

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
