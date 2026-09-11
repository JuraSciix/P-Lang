package plang.translator.codegen;

import plang.translator.Ast.*;

import static plang.interpreter.OPCodeList.*;

public class Gen extends Visitor {
    private final Code code;
    private final CodeEmitter emitter;
    private final ConstTable constTable;
    private final Items items;

    Items.Item resultItem;
    Items.Dest destItem;

    public Gen(Code code, CodeEmitter emitter) {
        this.code = code;
        this.emitter = emitter;
        constTable = new ConstTable();
        items = new Items(code, emitter, constTable);
    }

    public CodeData getData() {
        return new CodeData(
                emitter.getCodeArray(),
                constTable.getPoolArray()
        );
    }

    public Items.Item gen(Stmt stmt) {
        return gen(stmt, items.direct());
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
        resultItem = items.graph().aliveness(alive);
    }

    @Override
    public void conditional(Conditional tree) {
        Mark test = gen(tree.test).toCond(null).emitFalseJump();
        boolean thenAlive = gen(tree.body).use().alive();
        if (tree.elseBody != null) {
            Mark exitMark = code.jump(jump, null);
            code.close(test);
            boolean elseAlive = gen(tree.elseBody).use().alive();
            code.close(exitMark);
            resultItem = items.graph().aliveness(thenAlive || elseAlive);
        } else {
            code.close(test);
            resultItem = items.graph();
        }
    }

    @Override
    public void whileLoop(WhileLoop tree) {
        int startBci = emitter.top();
        Mark test = gen(tree.test).toCond(null).emitFalseJump();
        gen(tree.body).use();
        code.close(code.jump(jump, null), startBci);
        code.close(test);
        resultItem = items.graph();
    }

    @Override
    public void returnOp(Return tree) {
        Items.Item item = (tree.expr != null) ? gen(tree.expr) : items.direct().storeConst(0L);
        emitter.opcodeWithByteIndex(ret, item.use().index());
        resultItem = items.graph().aliveness(false);
    }

    @Override
    public void asg(Asg tree) {
        Items.StableItem item;
        if (tree.name.hasItem()) {
            item = tree.name.item();
        } else {
            item = items.stableItem();
            tree.name.setItem(item);
        }

        gen(tree.expr, items.stable(item)).use();
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

            default: {
                Items.Item lhsItem = gen(tree.lhs);
                Items.Item rhsItem = gen(tree.rhs);

                int lhsIndex = lhsItem.use().index();
                int rhsIndex = rhsItem.use().index();

                int opcode = AstInfo.opcodeFromTag(tree.tag);
                if (AstInfo.isComparing(tree.tag)) {
                    emitter.opcodeWithDoubleByteIndex(opcode, lhsIndex, rhsIndex);
                    resultItem = items.cond(destItem);
                } else {
                    Items.Item dest = destItem.prepare();
                    emitter.opcodeWithTripleByteIndex(opcode, lhsIndex, rhsIndex, dest.index());
                    resultItem = dest;
                }
            }
        }
    }

    @Override
    public void unaryOp(UnaryOp tree) {
        Items.Item item = gen(tree.expr);
        switch (tree.tag) {
            case NOT:
                resultItem = item.toCond(destItem).negate();
                break;
            case NEG:
                int index = item.use().index();
                Items.Item dest = destItem.prepare();
                emitter.opcodeWithDoubleByteIndex(neg, index, dest.index());
                resultItem = dest;
                break;
        }
    }

    @Override
    public void value(Value tree) {
        switch (tree.tag) {
            case VAR: {
                Name name = (Name) tree.value;
                if (!name.hasItem()) {
                    throw new IllegalArgumentException("Unresolved variable " + name);
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
