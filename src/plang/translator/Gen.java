package plang.translator;

import plang.translator.Ast.*;
import plang.translator.Items.*;

import static plang.interpreter.OPCodeList.*;

public class Gen extends Visitor {
    private final Code code;
    private final Items items;
    private final LocalTable localTable;
    private final ConstTable constTable;
    Item receivingItem;
    Item genItem;

    public Gen(Code code) {
        this.code = code;
        items = new Items(code);
        localTable = new LocalTable();
        constTable = new ConstTable();
    }

    Item gen(Stmt stmt) {
        return gen(stmt, items.dynamicItem());
    }

    Item gen(Stmt stmt, Item receiver) {
        Item prevReceivingItem = receivingItem;
        Item prevGenItem = genItem;

        try {
            receivingItem = receiver;
            stmt.accept(this);
            return genItem;
        } finally {
            receivingItem = prevReceivingItem;
            genItem = prevGenItem;
        }
    }

    @Override
    public void visitCompound(Compound stmt) {
        for (Stmt child : stmt.children) {
            gen(child).dispose();
        }
        genItem = items.operItem();
    }

    @Override
    public void visitReturn(Return stmt) {
        if (stmt.expr == null) {
            code.emit(leave);
        } else {
            ValueItem item = gen(stmt.expr).load();
            code.emit1(_return, item.index);
        }

        genItem = items.operItem();
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

        LocalItem item = items.localItem(index);
        gen(stmt.expr, item).load(item.index);
        genItem = items.operItem();
    }

    @Override
    public void visitBinaryOp(BinaryOp stmt) {
        ValueItem lhs = gen(stmt.lhs).load();
        ValueItem rhs = gen(stmt.rhs).load();
        ValueItem result = receivingItem.load();
        code.emitPos(stmt.pos);
        code.emitBinary(AstInfo.opcodeFromTag(stmt.tag), lhs.index, rhs.index, result.index);
        lhs.dispose();
        rhs.dispose();
        genItem = result;
    }

    @Override
    public void visitUnaryOp(UnaryOp stmt) {
        ValueItem item = gen(stmt.expr).load();
        int resultIndex = code.allocReg();
        code.emitPos(stmt.pos);
        code.emitUnary(AstInfo.opcodeFromTag(stmt.tag), item.index, resultIndex);
        item.dispose();
        genItem = items.valueItem(resultIndex);
    }

    @Override
    public void visitValue(Value stmt) {
        switch (stmt.tag) {
            case VAR: {
                String name = (String) stmt.value;
                int localIndex = localTable.resolve(name);
                genItem = items.localItem(localIndex);
                break;
            }

            case INT: {
                int i = (int) stmt.value;
                int constIndex = constTable.lookup(i);
                genItem = items.constItem(constIndex);
                break;
            }

            default:
                throw new AssertionError(stmt.tag);
        }
    }

    @Override
    public void visitParens(Parens stmt) {
        stmt.expr.accept(this);
    }
}
