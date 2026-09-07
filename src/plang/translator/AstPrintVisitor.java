package plang.translator;

import java.io.PrintWriter;
import plang.translator.Ast.*;

public class AstPrintVisitor extends Ast.Visitor {
    private final PrintWriter output = new PrintWriter(System.out);

    public void flush() {
        output.flush();
    }

    @Override
    public void visitCompound(Compound tree) {
        output.println("COMPOUND {");
        for (Stmt child : tree.children) {
            child.accept(this);
        }
        output.println("}");
    }

    @Override
    public void visitAsg(Asg tree) {
        output.print("VAR ");
        output.print(tree.name);
        output.print(" = ");
        tree.expr.accept(this);
    }

    @Override
    public void visitBinaryOp(BinaryOp tree) {
        output.println("BINARY-OP");
        tree.lhs.accept(this);
        output.println(tree.getTag());
        tree.rhs.accept(this);
    }

    @Override
    public void visitUnaryOp(UnaryOp tree) {
        output.print("UNARY-OP ");
        output.println(tree.getTag());
        tree.expr.accept(this);
    }

    @Override
    public void visitIf(If tree) {
        output.println("IF (");
        tree.cond.accept(this);
        output.println(")");
        tree.body.accept(this);
        if (tree.elseBody != null) {
            output.println("ELSE");
            tree.elseBody.accept(this);
        }
    }

    @Override
    public void visitWhile(While stmt) {
        output.println("WHILE (");
        stmt.cond.accept(this);
        output.println(")");
        stmt.body.accept(this);
    }

    @Override
    public void visitReturn(Return tree) {
        if (tree.expr == null) {
            output.print("RETURN NONE");
        } else {
            output.println("RETURN");
            tree.expr.accept(this);
        }
    }

    @Override
    public void visitValue(Value tree) {
        output.print("VALUE ");
        output.print(tree.getTag());
        output.print(" ");
        output.println(tree.value);
    }

    @Override
    public void visitParens(Parens tree) {
        output.println("PARENS (");
        tree.expr.accept(this);
        output.println(")");
    }
}
