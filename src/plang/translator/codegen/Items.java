package plang.translator.codegen;

import static plang.interpreter.OPCodeList.*;

public class Items {
    private final Code code;
    private final CodeEmitter emitter;
    private final ConstTable constTable;
    private final Arena arena;
    private final DirectDest directDest;

    Items(Code code, CodeEmitter emitter, ConstTable constTable, Arena arena) {
        this.code = code;
        this.emitter = emitter;
        this.constTable = constTable;
        this.arena = arena;

        directDest = new DirectDest();
    }

    DirectDest direct() {
        return directDest;
    }

    StableDest stable(StableItem item) {
        return new StableDest(item);
    }

    StableItem stableItem() {
        return new StableItem(arena.acquire());
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
                emitter.opcodeWithByteIndex(const_0 + (int) value, item.index());
            } else {
                emitter.opcodeWithShortIndexAndByteIndex(load, constTable.lookup(value), item.index());
            }
            return item;
        }
    }

    /**
     * Слот переменной. Запись будет осуществляться в переменную.
     */
    class StableDest extends Dest {
        final StableItem item;

        StableDest(StableItem item) {
            this.item = item;
        }

        @Override
        Item prepare() {
            return item;
        }

        @Override
        Item storeStable(int stableIndex) {
            emitter.opcodeWithDoubleByteIndex(mov, stableIndex, item.index());
            return new StableItem(stableIndex);
        }

        @Override
        Item storeConst(long value) {
            if (-1L <= value && value <= 2L) {
                emitter.opcodeWithByteIndex(const_0 + (int) value, item.index());
            } else {
                emitter.opcodeWithShortIndexAndByteIndex(load, constTable.lookup(value), item.index);
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
            int unitIndex = arena.acquire();
            use();
            emitter.opcodeWithByteIndex(const_0, unitIndex);
            emitter.opcodeWithDoubleByteIndex(cmp_ne, index(), unitIndex);
            arena.release(unitIndex);
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
            super(arena.acquire());
        }

        @Override
        Item use() {
            arena.release(index());
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

        CondItem negate() {
            opcode = OPCodes.negate(opcode);
            Mark tmp = falseMarks;
            falseMarks = trueMarks;
            trueMarks = tmp;
            return this;
        }

        Mark emitFalseJump() {
            Mark mark = code.jump(opcode, falseMarks);
            code.close(trueMarks);
            return mark;
        }

        Mark emitTrueJump() {
            Mark mark = code.jump(OPCodes.negate(opcode), trueMarks);
            code.close(falseMarks);
            return mark;
        }

        CondItem coalesceTrueJumps(Mark trueMarks) {
            this.trueMarks = Mark.merge(this.trueMarks, trueMarks);
            return this;
        }

        CondItem coalesceFalseJumps(Mark falseMarks) {
            this.falseMarks = Mark.merge(this.falseMarks, falseMarks);
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
            Mark elseMark = code.jump(opcode, falseMarks);
            code.close(trueMarks);
            emitter.opcodeWithByteIndex(const_1, item.index());
            Mark exitMark = code.jump(jump, null);
            code.close(elseMark);
            emitter.opcodeWithByteIndex(const_0, item.index());
            code.close(exitMark);
            return item;
        }
    }
}
