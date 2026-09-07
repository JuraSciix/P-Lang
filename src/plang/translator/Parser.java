package plang.translator;

import plang.translator.Ast.*;
import plang.translator.Ast.Expr.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static plang.translator.ParseHelper.*;
import static plang.translator.TokenType.*;

public class Parser {

    // Рекурсивный спуск: LR(1)

    public ParserResult parse(LexResult lexResult) {
        List<Stmt> statements = new ArrayList<>();
        TokenReader reader = new TokenReader(lexResult.getTokens(), lexResult.getData());

        while (reader.hasRemaining()) {
            Token tk = reader.currentToken();
            if (tk.hasType(SEP)) {
                reader.step();
                continue;
            }
            statements.add(parseStmt(reader));
        }

        return new ParserResult(
                lexResult.getSourceName(),
                lexResult.getLineNumberMap(),
                Collections.singletonList(new Compound(0, statements))
        );
    }

    private Stmt parseStmt(TokenReader reader) {
        Token tk = reader.currentToken();

        if (tk.hasType(LBRACE)) {
            reader.step();
            List<Stmt> children = new ArrayList<>();
            while (reader.hasRemaining()) {
                Token tk1 = reader.currentToken();
                if (tk1.hasType(SEP)) {
                    reader.step();
                    continue;
                }
                if (tk1.hasType(RBRACE)) {
                    reader.step();
                    break;
                }
                children.add(parseStmt(reader));
            }
            return new Compound(tk.pos, children);
        }

        if (tk.hasType(IF)) {
            reader.step();
            expect(reader, LPAREN);
            Expr expr = parseExpr(reader);
            expect(reader, RPAREN);
            Stmt thenBody = parseStmt(reader);
            Stmt elseBody = null;
            if (reader.hasRemaining()) {
                Token tk1 = reader.currentToken();
                if (tk1.hasType(ELSE)) {
                    reader.step();
                    elseBody = parseStmt(reader);
                }
            }
            return new If(tk.pos, expr, thenBody, elseBody);
        }

        if (tk.hasType(WHILE)) {
            reader.step();
            expect(reader, LPAREN);
            Expr expr = parseExpr(reader);
            expect(reader, RPAREN);
            Stmt body = parseStmt(reader);
            return new While(tk.pos, expr, body);
        }

        if (tk.hasType(RETURN)) {
            reader.step();
            Expr expr = null;
            if (reader.hasRemaining()) {
                Token tk1 = reader.currentToken();
                if (!tk1.hasType(SEP)) {
                    expr = parseExpr(reader);
                }
            }
            return new Return(tk.pos, expr);
        }

        // Один из немногих случаев, когда нужно посмотреть сразу два токена вперед:
        if (tk.hasType(IDENTIFIER)) {
            reader.step();
            if (reader.hasRemaining()) {
                Token tk1 = reader.currentToken();
                if (tk1.hasType(ASG)) {
                    reader.step();
                    String name = reader.getTokenData(tk);
                    Expr expr = parseExpr(reader);
                    return new Asg(tk1.pos, name, expr);
                }
            }
            reader.stepBack();
        }

        return parseExpr(reader);
    }

    // Приоритеты операций:
    // +, -
    // %
    // *, /
    // - (унарный), + (унарный)
    // Переменная
    // Скобки

    private Expr parseExpr(TokenReader reader) {
        return parseBinary(reader);
    }

    private Expr parseBinary(TokenReader reader) {
        // Look-ahead
        Expr lhs = parseUnary(reader);
        BinaryOp last = null;

        while (reader.hasRemaining()) {
            Token tk = reader.currentToken();
            if (!isTokenTypeOfBinary(tk.type)) {
                break;
            }
            reader.step();
            Expr rhs = parseUnary(reader);
            Tag tag = tagOf(tk.type);
            if (last != null && precedence(last.tag) > precedence(tag)) {
                // Перестраиваем дерево:
                // (a + b) * c = a + (b * c)
                BinaryOp newRhs = new BinaryOp(tk.pos, tag, last.rhs, rhs);
                last = new BinaryOp(last.pos, last.tag, last.lhs, newRhs);
            } else {
                last = new BinaryOp(tk.pos, tag, lhs, rhs);
            }
            lhs = last;
        }

        return lhs;
    }

    private Expr parseUnary(TokenReader reader) {
        Token tk = reader.currentToken();

        if (tk.hasType(MINUS)) {
            reader.step();
            Expr expr = parseUnary(reader);
            return new UnaryOp(tk.pos, Tag.NEG, expr);
        }

        if (tk.hasType(BANG)) {
            reader.step();
            Expr expr = parseUnary(reader);
            return new UnaryOp(tk.pos, Tag.NOT, expr);
        }

        return parseLowest(reader);
    }

    private Expr parseLowest(TokenReader reader) {
        Token tk = reader.currentToken();

        if (tk.hasType(IDENTIFIER)) {
            reader.step();
            String name = reader.getTokenData(tk);
            return new Value(tk.pos, Tag.VAR, name);
        }

        if (tk.hasType(INTEGER)) {
            reader.step();
            int value = Integer.parseInt(reader.getTokenData(tk));
            return new Value(tk.pos, Tag.INT, value);
        }

        if (tk.hasType(LPAREN)) {
            reader.step();
            Expr expr = parseExpr(reader);
            expect(reader, RPAREN);
            return new Parens(tk.pos, expr);
        }

        throw new IllegalArgumentException("Unexpected token: " + tk.type + " at " + tk.pos);
    }

    private void expect(TokenReader reader, TokenType type) {
        if (reader.hasRemaining()) {
            Token tk = reader.currentToken();
            if (tk.hasType(type)) {
                reader.step();
                return;
            }

            throw new IllegalArgumentException("Expected token " + type + ", but reached " + tk.type + " at " + tk.pos);
        }

        throw new IllegalArgumentException("Expected token " + type + ", but reached end of file");
    }
}
