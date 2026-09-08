package plang.translator;

import java.io.PrintWriter;
import plang.translator.Ast.*;

public class AstPrintVisitor extends Ast.Visitor {
    private final PrintWriter output = new PrintWriter(System.out);

    public void flush() {
        output.flush();
    }

    @Override
    public void compound(Compound tree) {
        output.println("COMPOUND {");
        for (Stmt child : tree.children) {
            child.accept(this);
        }
        output.println("}");
    }

    @Override
    public void asg(Asg tree) {
        output.print("VAR ");
        output.print(tree.name);
        output.print(" = ");
        tree.expr.accept(this);
    }

    @Override
    public void binaryOp(BinaryOp tree) {
        output.println("BINARY-OP");
        tree.lhs.accept(this);
        output.println(tree.getTag());
        tree.rhs.accept(this);
    }

    @Override
    public void unaryOp(UnaryOp tree) {
        output.print("UNARY-OP ");
        output.println(tree.getTag());
        tree.expr.accept(this);
    }

    @Override
    public void conditional(Conditional tree) {
        output.println("IF (");
        tree.test.accept(this);
        output.println(")");
        tree.body.accept(this);
        if (tree.elseBody != null) {
            output.println("ELSE");
            tree.elseBody.accept(this);
        }
    }

    @Override
    public void whileLoop(WhileLoop tree) {
        output.println("WHILE (");
        tree.test.accept(this);
        output.println(")");
        tree.body.accept(this);
    }

    @Override
    public void returnOp(Return tree) {
        if (tree.expr == null) {
            output.print("RETURN NONE");
        } else {
            output.println("RETURN");
            tree.expr.accept(this);
        }
    }

    @Override
    public void value(Value tree) {
        output.print("VALUE ");
        output.print(tree.getTag());
        output.print(" ");
        output.println(tree.value);
    }

    @Override
    public void parens(Parens tree) {
        output.println("PARENS (");
        tree.expr.accept(this);
        output.println(")");
    }
}
