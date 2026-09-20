package plang.translator.codegen;

import plang.interpreter.OPCodeList;
import plang.translator.Ast.*;
import plang.translator.TranslatorException;

import static plang.interpreter.OPCodeList.*;
import static plang.translator.codegen.AstInfo.opcodeFromTag;

public class Gen extends Visitor {
    private final Code mCode;
    private final Items mItems;

    Items.Item resultItem;
    Items.Dest destItem;

    public Gen(Code code) {
        mCode = code;
        mItems = new Items(code);
    }

    public Items.Item gen(Stmt stmt) {
        return gen(stmt, mItems.directDest());
    }

    public Items.Item gen(Stmt stmt, Items.Dest dest) {
        Items.Dest prevDest = destItem;
        Items.Item prevResult = resultItem;

        try {
            destItem = dest;
            resultItem = null;
            stmt.accept(this);
            return resultItem;
        } finally {
            destItem = prevDest;
            resultItem = prevResult;
        }
    }

    @Override
    public void compound(Compound tree) {
        boolean alive = true;
        for (Stmt child : tree.children) {
            if (!gen(child).use().alive()) {
                alive = false;
                break;
            }
        }
        resultItem = mItems.graph().aliveness(alive);
    }

    @Override
    public void conditional(Conditional tree) {
        Mark test = gen(tree.test).toCond(null).emitFalseJump();
        boolean thenAlive = gen(tree.body).use().alive();
        if (tree.elseBody != null) {
            Mark exitMark = mCode.jump(jump, null);
            mCode.close(test);
            boolean elseAlive = gen(tree.elseBody).use().alive();
            mCode.close(exitMark);
            resultItem = mItems.graph().aliveness(thenAlive || elseAlive);
        } else {
            mCode.close(test);
            resultItem = mItems.graph();
        }
    }

    @Override
    public void whileLoop(WhileLoop tree) {
        int startBci = mCode.emitter().top();
        Mark test = gen(tree.test).toCond(null).emitFalseJump();
        gen(tree.body).use();
        mCode.close(mCode.jump(jump, null), startBci);
        mCode.close(test);
        resultItem = mItems.graph();
    }

    @Override
    public void returnOp(Return tree) {
        Items.StableDest ret = mItems.retDest();
        // Заметка: вызывать .use() не обязательно,
        // так как item это всегда StableItem.
        if (tree.expr != null) {
            gen(tree.expr, ret);
        } else {
            ret.storeConst(0L);
        }
        mCode.emitter().opcode(OPCodeList.ret);
        resultItem = mItems.graph().aliveness(false);
    }

    @Override
    public void asg(Asg tree) {
        Items.StableItem item;
        if (tree.name.hasItem()) {
            item = tree.name.item();
        } else {
            item = mItems.stable();
            tree.name.setItem(item);
        }

        gen(tree.expr, mItems.stableDest(item)).use();
        resultItem = item;
    }

    @Override
    public void binaryOp(BinaryOp tree) {
        switch (tree.tag) {
            case CON: {
                Mark falseJumps = gen(tree.lhs).toCond(null).emitFalseJump();
                resultItem = gen(tree.rhs).toCond(destItem).coalesceFalseJumps(falseJumps);
                break;
            }

            case DIS: {
                Mark trueJumps = gen(tree.lhs).toCond(null).emitTrueJump();
                resultItem = gen(tree.rhs).toCond(destItem).coalesceTrueJumps(trueJumps);
                break;
            }

            case CMP_EQ: case CMP_NE:
            case CMP_LT: case CMP_GE:
            case CMP_GT: case CMP_LE: {
                Items.Item lhsItem = gen(tree.lhs);
                Items.Item rhsItem = gen(tree.rhs);
                int lhsIndex = lhsItem.use().index();
                int rhsIndex = rhsItem.use().index();
                mCode.emitter().opcodeWithDoubleByteIndex(opcodeFromTag(tree.tag), lhsIndex, rhsIndex);
                resultItem = mItems.cond(destItem);
                break;
            }

            default: {
                Items.Item lhsItem = gen(tree.lhs, destItem.safe());
                Items.Item rhsItem = gen(tree.rhs);
                int lhsIndex = lhsItem.index();
                int rhsIndex = rhsItem.use().index();
                mCode.emitter().opcodeWithDoubleByteIndex(opcodeFromTag(tree.tag), lhsIndex, rhsIndex);
                resultItem = lhsItem;
            }
        }
    }

    @Override
    public void unaryOp(UnaryOp tree) {
        Items.Item item = gen(tree.expr, destItem);
        switch (tree.tag) {
            case NOT:
                resultItem = item.toCond(destItem).negate();
                break;
            case NEG:
                int index = item.index();
                mCode.emitter().opcodeWithByteIndex(neg, index);
                resultItem = item;
                break;
        }
    }

    @Override
    public void value(Value tree) {
        switch (tree.tag) {
            case VAR: {
                Name name = (Name) tree.value;
                if (!name.hasItem()) {
                    throw new TranslatorException("Unresolved variable " + name);
                }
                resultItem = destItem.storeStable(name.item().index());
                break;
            }

            case INT: {
                long value = (long) tree.value;
                resultItem = destItem.storeConst(value);
                break;
            }
        }
    }

    @Override
    public void parens(Parens tree) {
        resultItem = gen(tree.expr);
    }
}
