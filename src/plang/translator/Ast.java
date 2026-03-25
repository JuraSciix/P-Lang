package plang.translator;

import java.util.List;

public interface Ast {

    class Visitor {
        public void visitCompound(Compound stmt) { visitStmt(stmt); }
        public void visitAsg(Asg stmt) { visitStmt(stmt); }
        public void visitBinaryOp(BinaryOp stmt) { visitStmt(stmt); }
        public void visitUnaryOp(UnaryOp stmt) { visitStmt(stmt); }
        public void visitReturn(Return stmt) { visitStmt(stmt); }
        public void visitIf(If stmt) { visitStmt(stmt); }
        public void visitValue(Value stmt) { visitStmt(stmt); }
        public void visitParens(Parens stmt) { visitStmt(stmt); }
        public void visitStmt(Stmt stmt) { assert stmt != null; }
    }

    /**
     * Примечательно, что между позицией и узлом АСД сохраняется биекция.
     * По этой причине, позицию можно использовать как ключ.
     */
    abstract class Stmt {
        public final int pos;

        protected Stmt(int pos) {
            this.pos = pos;
        }

        public abstract void accept(Visitor visitor);
    }

    final class Compound extends Stmt {
        public final List<Stmt> children;

        public Compound(int pos, List<Stmt> children) {
            super(pos);
            this.children = children;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCompound(this);
        }
    }

    final class Asg extends Stmt {
        public final String name;
        public final Expr expr;

        public Asg(int pos, String name, Expr expr) {
            super(pos);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAsg(this);
        }
    }

    abstract class Expr extends Stmt {
        public enum Tag {
            ADD,
            SUB,
            MUL,
            DIV,
            REM,
            NEG,
            PARENS,
            INT,
            VAR,
            IF,
            RETURN
        }

        protected Expr(int pos) {
            super(pos);
        }

        public abstract Tag getTag();
    }

    final class If extends Expr {
        public final Expr condition;
        public final Stmt thenBody;
        public final Stmt elseBody;

        public If(int pos, Expr condition, Stmt thenBody, Stmt elseBody) {
            super(pos);
            this.condition = condition;
            this.thenBody = thenBody;
            this.elseBody = elseBody;
        }

        @Override
        public Tag getTag() { return Tag.IF; }

        @Override
        public void accept(Visitor visitor) { visitor.visitIf(this); }
    }

    class BinaryOp extends Expr {
        public final Tag tag;
        public final Expr lhs, rhs;

        public BinaryOp(int pos, Tag tag, Expr lhs, Expr rhs) {
            super(pos);
            this.tag = tag;
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitBinaryOp(this); }
    }

    class UnaryOp extends Expr {
        public final Tag tag;
        public final Expr expr;

        public UnaryOp(int pos, Tag tag, Expr expr) {
            super(pos);
            this.tag = tag;
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitUnaryOp(this); }
    }

    class Return extends Expr {
        public final Expr expr;

        public Return(int pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.RETURN; }

        @Override
        public void accept(Visitor visitor) { visitor.visitReturn(this); }
    }

    class Value extends Expr {
        public final Tag tag;
        public final Object value;

        public Value(int pos, Tag tag, Object value) {
            super(pos);
            this.tag = tag;
            this.value = value;
        }

        @Override
        public Tag getTag() { return tag; }

        @Override
        public void accept(Visitor visitor) { visitor.visitValue(this); }
    }

    class Parens extends Expr {
        public final Expr expr;

        public Parens(int pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public Tag getTag() { return Tag.PARENS; }

        @Override
        public void accept(Visitor visitor) { visitor.visitParens(this); }
    }
}
