package plang.translator.codegen;

import plang.translator.Ast.*;

import static plang.interpreter.OPCodeList.*;

public class Gen extends Visitor {
    private final Code code;
    private final CodeEmitter emitter;
    private final LocalTable localTable;
    private final ConstTable constTable;
    private final Items items;

    Items.Item resultItem;
    Items.Dest destItem;

    public Gen(Code code, CodeEmitter emitter) {
        this.code = code;
        this.emitter = emitter;
        localTable = new LocalTable();
        constTable = new ConstTable();
        items = new Items(code, emitter, constTable);

        destItem = items.direct();
    }

    public CodeData getData() {
        return new CodeData(
                emitter.getCodeArray(),
                constTable.getPoolArray()
        );
    }

    Items.Item gen(Stmt stmt) {
        return gen(stmt, items.direct());
    }

    Items.Item gen(Stmt stmt, Items.Dest dest) {
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
    public void visitCompound(Compound stmt) {
        for (Stmt child : stmt.children) {
            gen(child).use();
        }
        resultItem = items.empty();
    }

    @Override
    public void visitIf(If stmt) {
        boolean hasElse = (stmt.elseBody != null);
        Items.CondItem cond = gen(stmt.cond).cond(null);
        cond.emitFalseJump();
        cond.closeTrue();
        gen(stmt.body).use();
        if (hasElse) {
            Mark exitMark = emitter.mark(jump, null);
            cond.closeFalse();
            gen(stmt.elseBody).use();
            emitter.close(exitMark);
        } else {
            cond.closeFalse();
        }
        resultItem = items.empty();
    }

    @Override
    public void visitWhile(While stmt) {
        int startBci = emitter.top();
        Items.CondItem cond = gen(stmt.cond).cond(null);
        cond.emitFalseJump();
        gen(stmt.body).use();
        emitter.emitBS(jump, startBci);
        cond.closeFalse();
        resultItem = items.empty();
    }

    @Override
    public void visitReturn(Return stmt) {
        Items.Item item = (stmt.expr != null) ? gen(stmt.expr) : items.direct().storeConst(0L);
        emitter.emitBB(ret, item.use());
        resultItem = items.empty();
    }

    @Override
    public void visitAsg(Asg stmt) {
        int index;
        if (localTable.contains(stmt.name)) {
            index = localTable.resolve(stmt.name);
        } else {
            index = code.acquire();
            localTable.register(stmt.name, index);
        }

        Items.Dest stable = items.stableDest(index);
        gen(stmt.expr, stable).use();
        resultItem = stable.prepare();
    }

    @Override
    public void visitBinaryOp(BinaryOp stmt) {
        switch (stmt.tag) {
            case CON: {
                Items.CondItem lhsCond = gen(stmt.lhs).cond(null);
                lhsCond.emitFalseJump();
                lhsCond.closeTrue();
                Items.CondItem rhsCond = gen(stmt.rhs).cond(null);
                resultItem = items.cond(destItem).inherit(
                        rhsCond.opcode,
                        rhsCond.trueMarks,
                        Mark.merge(lhsCond.falseMarks, rhsCond.falseMarks));
                break;
            }

            case DIS: {
                Items.CondItem lhsCond = gen(stmt.lhs).cond(null);
                lhsCond.emitTrueJump();
                lhsCond.closeFalse();
                Items.CondItem rhsCond = gen(stmt.rhs).cond(null);
                resultItem = items.cond(destItem).inherit(
                        rhsCond.opcode,
                        Mark.merge(lhsCond.trueMarks, rhsCond.trueMarks),
                        rhsCond.falseMarks);
                break;
            }

            default: {
                Items.Item lhsItem = gen(stmt.lhs);
                Items.Item rhsItem = gen(stmt.rhs);

                int lhsIndex = lhsItem.use();
                int rhsIndex = rhsItem.use();

                int opcode = AstInfo.opcodeFromTag(stmt.tag);
                if (AstInfo.isComparing(stmt.tag)) {
                    emitter.emitBBB(opcode, lhsIndex, rhsIndex);
                    resultItem = items.cond(destItem);
                } else {
                    Items.Item dest = destItem.prepare();
                    emitter.emitBBBB(opcode, lhsIndex, rhsIndex, dest.index());
                    resultItem = dest;
                }
            }
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp stmt) {
        Items.Item item = gen(stmt.expr);
        switch (stmt.tag) {
            case NOT:
                resultItem = item.cond(destItem).negate();
                break;
            case NEG:
                int index = item.use();
                Items.Item dest = destItem.prepare();
                emitter.emitBBB(neg, index, dest.index());
                resultItem = dest;
                break;
        }
    }

    @Override
    public void visitValue(Value stmt) {
        switch (stmt.tag) {
            case VAR: {
                String name = (String) stmt.value;
                resultItem = destItem.storeStable(localTable.resolve(name));
                break;
            }

            case INT: {
                long value = (long) stmt.value;
                resultItem = destItem.storeConst(value);
                break;
            }
        }
    }

    @Override
    public void visitParens(Parens stmt) {
        stmt.expr.accept(this);
    }
}
