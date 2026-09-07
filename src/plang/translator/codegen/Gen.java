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
        Item presDest = destItem;
        Item prevResult = resultItem;

        try {
            destItem = dest;
            stmt.accept(this);
            return resultItem;
        } finally {
            destItem = presDest;
            resultItem = prevResult;
        }
    }

    @Override
    public void visitCompound(Compound stmt) {
        for (Stmt child : stmt.children) {
            child.accept(this);
        }
    }

    @Override
    public void visitIf(If stmt) {
        Items.CondItem cond = gen(stmt.condition).cond();


        emitter.emitBS(cond.opcode, 0);
        int pc0 = emitter.top() - 2;

        gen(stmt.thenBody);

        if (stmt.elseBody == null) {
            emitter.s(pc0, emitter.top());
        } else {
            emitter.emitBS(jump, 0);
            int pc1 = emitter.top() - 2;

            emitter.s(pc0, emitter.top());

            gen(stmt.elseBody);

            emitter.s(pc1, emitter.top());
        }
    }

    @Override
    public void visitWhile(While stmt) {
        int pc0 = emitter.top();

        Items.CondItem cond = gen(stmt.condition).cond();

        emitter.emitBS(cond.opcode, 0);
        int pc1 = emitter.top() - 2;

        gen(stmt.body);

        emitter.emitBS(jump, pc0);

        emitter.s(pc1, emitter.top());
    }

    @Override
    public void visitReturn(Return stmt) {
        if (stmt.expr == null) {
            emitter.emit(leave);
        } else {
            Item item = gen(stmt.expr);
            emitter.emitBB(_return, item.use());
        }
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

        gen(stmt.expr, items.stable(index));
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
            resultItem = items.cond(jmp_z);
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
                resultItem = item.cond().negate();
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
                int index = (int) stmt.value;
                resultItem = destItem.storeConst(constTable.lookup(index));
                break;
            }
        }
    }

    @Override
    public void visitParens(Parens stmt) {
        stmt.expr.accept(this);
    }
}
