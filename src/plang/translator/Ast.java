package plang.translator;

import plang.translator.codegen.Name;

import java.util.List;

public interface Ast {

    class Visitor {
        public void compound(Compound tree) { tree(tree); }
        public void conditional(Conditional tree) { tree(tree); }
        public void whileLoop(WhileLoop tree) { tree(tree); }
        public void returnOp(Return tree) { tree(tree); }
        public void asg(Asg tree) { tree(tree); }
        public void binaryOp(BinaryOp tree) { tree(tree); }
        public void unaryOp(UnaryOp tree) { tree(tree); }
        public void value(Value tree) { tree(tree); }
        public void parens(Parens tree) { tree(tree); }
        public void tree(Stmt stmt) { assert stmt != null; }
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
            visitor.compound(this);
        }
    }

    final class Conditional extends Stmt {
        public final Expr test;
        public final Stmt body;
        public final Stmt elseBody;

        public Conditional(int pos, Expr test, Stmt body, Stmt elseBody) {
            super(pos);
            this.test = test;
            this.body = body;
            this.elseBody = elseBody;
        }

        @Override
        public void accept(Visitor visitor) { visitor.conditional(this); }
    }

    final class WhileLoop extends Stmt {
        public final Expr test;
        public final Stmt body;

        public WhileLoop(int pos, Expr test, Stmt body) {
            super(pos);
            this.test = test;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) { visitor.whileLoop(this); }
    }

    class Return extends Stmt {
        public final Expr expr;

        public Return(int pos, Expr expr) {
            super(pos);
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) { visitor.returnOp(this); }
    }

    final class Asg extends Stmt {
        public final Name name;
        public final Expr expr;

        public Asg(int pos, Name name, Expr expr) {
            super(pos);
            this.name = name;
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.asg(this);
        }
    }

    abstract class Expr extends Stmt {
        public enum Tag {
            NOP,
            ADD,
            SUB,
            MUL,
            DIV,
            REM,
            BIT_AND,
            BIT_OR,
            BIT_XOR,
            BIT_INV,
            NOT,
            NEG,
            CMP_EQ,
            CMP_NE,
            CMP_LT,
            CMP_GE,
            CMP_GT,
            CMP_LE,
            CON,
            DIS,
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
        public void accept(Visitor visitor) { visitor.binaryOp(this); }
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
        public void accept(Visitor visitor) { visitor.unaryOp(this); }
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
        public void accept(Visitor visitor) { visitor.value(this); }
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
        public void accept(Visitor visitor) { visitor.parens(this); }
    }
}
