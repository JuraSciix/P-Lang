package plang.translator.codegen;

import plang.translator.Ast.*;
import plang.translator.codegen.Items.Item;

import static plang.interpreter.OPCodeList.*;

public class Gen extends Visitor {
    private final Code code;
    private final CodeEmitter emitter;
    private final LocalTable localTable;
    private final ConstTable constTable;
    private final Items items;

    Item resultItem;
    Item destItem;

    public Gen(Code code, CodeEmitter emitter) {
        this.code = code;
        this.emitter = emitter;
        localTable = new LocalTable();
        constTable = new ConstTable();
        items = new Items(code, emitter);

        destItem = items.direct();
    }

    public CodeData getData() {
        return new CodeData(
                emitter.getCodeArray(),
                constTable.getPoolArray()
        );
    }

    Item gen(Stmt stmt) {
        return gen(stmt, items.direct());
    }

    Item gen(Stmt stmt, Item dest) {
        Item prevDest = destItem;
        Item prevResult = resultItem;

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
        gen(stmt.body).use();
        if (hasElse) {
            cond.emitTrueJump();
        }
        cond.closeFalse();
        if (hasElse) {
            gen(stmt.elseBody).use();
            cond.closeTrue();
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
        if (stmt.expr == null) {
            emitter.emitByte(leave);
        } else {
            Item item = gen(stmt.expr);
            emitter.emitBB(_return, item.use());
        }
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

        Items.StableItem stable = items.stable(index);
        gen(stmt.expr, stable).use();
        resultItem = stable;
    }

    @Override
    public void visitBinaryOp(BinaryOp stmt) {
        int opcode = AstInfo.opcodeFromTag(stmt.tag);
        Item lhsItem = gen(stmt.lhs);
        Item rhsItem = gen(stmt.rhs);

        int lhsIndex = lhsItem.use();
        int rhsIndex = rhsItem.use();

        if (AstInfo.isComparing(stmt.tag)) {
            emitter.emitBBB(opcode, lhsIndex, rhsIndex);
            resultItem = items.cond(destItem);
        } else {
            Item dest = destItem.prepare();
            emitter.emitBBBB(opcode, lhsIndex, rhsIndex, dest.index());
            resultItem = dest;
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp stmt) {
        Item item = gen(stmt.expr);
        switch (stmt.tag) {
            case NOT:
                resultItem = item.cond(destItem).negate();
                break;
            case NEG:
                int index = item.use();
                Item dest = destItem.prepare();
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
                int value = (int) stmt.value;
                resultItem = destItem.storeConst(constTable.lookup(value));
                break;
            }
        }
    }

    @Override
    public void visitParens(Parens stmt) {
        stmt.expr.accept(this);
    }
}
