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

    public Gen(Code code, CodeEmitter emitter) {
        this.code = code;
        this.emitter = emitter;
        localTable = new LocalTable();
        constTable = new ConstTable();
        items = new Items(code, emitter);
    }

    public CodeData getData() {
        return new CodeData(
                emitter.getCodeArray(),
                constTable.getPoolArray()
        );
    }

    Item resultItem;
    Item destItem;

    Item gen(Stmt stmt) {
        return gen(stmt, items.dynamic());
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
        // todo
    }

    @Override
    public void visitWhile(While stmt) {
        // todo
    }

    @Override
    public void visitReturn(Return stmt) {
        if (stmt.expr == null) {
            emitter.emit(leave);
        } else {
            Item item = gen(stmt.expr, items.direct());
            emitter.emit1(_return, item.get());
        }
    }

    @Override
    public void visitAsg(Asg stmt) {
        int index;
        if (localTable.contains(stmt.name)) {
            index = localTable.resolve(stmt.name);
        } else {
            index = code.allocReg();
            localTable.register(stmt.name, index);
        }

        gen(stmt.expr, items.stable(index));
    }

    @Override
    public void visitBinaryOp(BinaryOp stmt) {
        int opcode = AstInfo.opcodeFromTag(stmt.tag);
        Item lhsItem = gen(stmt.lhs);
        Item rhsItem = gen(stmt.rhs);

        int lhsIndex = lhsItem.get();
        int rhsIndex = rhsItem.get();

        code.emitPos(stmt.pos);

        if (AstInfo.isComparing(stmt.tag)) {
            emitter.emit2(opcode, lhsIndex, rhsIndex);
            resultItem = items.cond();
        } else {
            Item dest = destItem.prepare();
            emitter.emitBinary(opcode, lhsIndex, rhsIndex, dest.get());
            resultItem = dest;
        }
    }

    @Override
    public void visitUnaryOp(UnaryOp stmt) {
        int opcode = AstInfo.opcodeFromTag(stmt.tag);
        Item item = gen(stmt.expr);
        int index = item.get();
        Item dest = destItem.prepare();
        emitter.emit(stmt.pos);
        emitter.emitUnary(opcode, index, dest.get());
        resultItem = dest;
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
                resultItem = destItem.storeConstant(constTable.lookup(index));
                break;
            }
        }
    }

    @Override
    public void visitParens(Parens stmt) {
        stmt.expr.accept(this);
    }
}
