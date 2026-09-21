package plang.translator.codegen;

import java.util.Objects;

import static plang.interpreter.OPCodeList.*;

public final class Items {
    private final Code mCode;
    private final DirectDest directDest;

    Items(Code code) {
        mCode = Objects.requireNonNull(code);
        directDest = new DirectDest();
    }

    DirectDest directDest() {
        return directDest;
    }

    StableDest stableDest(StableItem item) {
        return new StableDest(item);
    }

    StableDest retDest() {
        // Освобождаем нулевой регистр, чтобы новый item занял его.
        // ВАЖНО: после работы с этим Dest состояние нулевого регистра
        // должно быть возвращено.
        mCode.arena().reset(0, true);
        return stableDest(oneTime());
    }

    OneTimeItem oneTime() {
        return new OneTimeItem();
    }

    StableItem stable() {
        return new StableItem(mCode.arena().acquire());
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
    abstract class Dest {

        Dest safe() {
            return this;
        }

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
    public class DirectDest extends Dest {
        @Override
        Dest safe() {
            return stableDest(oneTime());
        }

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
            Item item = prepare();
            if (-1L <= value && value <= 2L) {
                mCode.emitter().opcodeWithByteIndex(const_0 + (int) value, item.index());
            } else {
                mCode.emitter().opcodeWithShortIndexAndByteIndex(load, mCode.constTable().lookup(value), item.index());
            }
            return item;
        }
    }

    /**
     * Слот переменной. Запись будет осуществляться в переменную.
     */
    class StableDest extends Dest {
        private final StableItem _item;

        StableDest(StableItem item) {
            _item = item;
        }

        @Override
        Item prepare() { return _item; }

        @Override
        Item storeStable(int stableIndex) {
            Item item = prepare();
            int destIndex = item.index();
            if (stableIndex != destIndex) {
                mCode.emitter().opcodeWithDoubleByteIndex(mov, stableIndex, destIndex);
            }
            return item;
        }

        @Override
        Item storeConst(long value) {
            Item item = prepare();
            if (-1L <= value && value <= 2L) {
                mCode.emitter().opcodeWithByteIndex(const_0 + (int) value, item.index());
            } else {
                mCode.emitter().opcodeWithShortIndexAndByteIndex(load, mCode.constTable().lookup(value), item.index());
            }
            return item;
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
        public Item use() {
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
        private boolean _alive = true;

        GraphItem aliveness(boolean alive) {
            _alive = alive;
            return this;
        }

        @Override
        public Item use() { return this; }

        @Override
        public boolean alive() { return _alive; }
    }

    /**
     * Стабильный слот. Это переменная. Индекс не освобождается.
     */
    class StableItem extends Item {
        private final int _index;

        StableItem(int index) {
            _index = index;
        }

        @Override
        CondItem toCond(Dest dest) {
            int unitIndex = mCode.arena().acquire();
            use();
            mCode.emitter().opcodeWithByteIndex(const_0, unitIndex);
            mCode.emitter().opcodeWithDoubleByteIndex(cmp_ne, index(), unitIndex);
            mCode.arena().release(unitIndex);
            return new CondItem(jmp_z, dest);
        }

        @Override
        public Item use() { return this; }

        @Override
        public int index() { return _index; }
    }

    /**
     * Одноразовый слот.
     * Служит для разовых операций, таких как сложение.
     * Индекс тоже одноразовый и становится недействительным после использования.
     */
    class OneTimeItem extends StableItem {
        OneTimeItem() {
            super(mCode.arena().acquire());
        }

        @Override
        public Item use() {
            mCode.arena().release(index());
            return this;
        }
    }

    /**
     * Слот над логическим значением.
     */
    class CondItem extends Item {
        private int _opcode;
        private Dest _dest;
        private Mark _trueJumps;
        private Mark _falseJumps;

        CondItem(int opcode, Dest dest) {
            _opcode = opcode;
            _dest = dest;
        }

        CondItem negate() {
            _opcode = OPCodes.negate(_opcode);
            Mark tmp = _falseJumps;
            _falseJumps = _trueJumps;
            _trueJumps = tmp;
            return this;
        }

        Mark emitFalseJump() {
            Mark mark = mCode.jump(_opcode, _falseJumps);
            mCode.close(_trueJumps);
            return mark;
        }

        Mark emitTrueJump() {
            Mark mark = mCode.jump(OPCodes.negate(_opcode), _trueJumps);
            mCode.close(_falseJumps);
            return mark;
        }

        CondItem coalesceTrueJumps(Mark trueJumps) {
            _trueJumps = Mark.merge(_trueJumps, trueJumps);
            return this;
        }

        CondItem coalesceFalseJumps(Mark falseJumps) {
            _falseJumps = Mark.merge(_falseJumps, falseJumps);
            return this;
        }

        @Override
        public CondItem toCond(Dest dest) {
            _dest = dest;
            return this;
        }

        @Override
        public Item use() {
            Item item = _dest.prepare();
            Mark elseMark = mCode.jump(_opcode, _falseJumps);
            mCode.close(_trueJumps);
            mCode.emitter().opcodeWithByteIndex(const_1, item.index());
            Mark exitMark = mCode.jump(jump, null);
            mCode.close(elseMark);
            mCode.emitter().opcodeWithByteIndex(const_0, item.index());
            mCode.close(exitMark);
            return item;
        }
    }
}
