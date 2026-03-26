package plang.translator;

import plang.translator.Ast.*;
import plang.translator.Ast.Expr.Tag;

import java.util.ArrayList;
import java.util.List;

import static plang.translator.TokenType.*;

public class Parser {

    // Рекурсивный спуск: LR(1)

    public ParserResult parse(LexerResult lexerResult) {
        List<Stmt> statements = new ArrayList<>();
        TokenReader reader = new TokenReader(lexerResult.getTokens(), lexerResult.getData());

        while (reader.hasRemaining()) {
            Token tk = reader.currentToken();
            if (tk.hasType(SEP)) {
                reader.step();
                continue;
            }
            statements.add(parseStmt(reader));
        }

        return new ParserResult(
                lexerResult.getSourceName(),
                lexerResult.getLineNumberMap(),
                statements
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
        return parseAdd(reader);
    }

    private Expr parseAdd(TokenReader reader) {
        // Look-ahead
        Expr lhs = parseRem(reader);

        while (reader.hasRemaining()) {
            Token tk = reader.currentToken();

            if (tk.hasType(PLUS)) {
                reader.step();
                Expr rhs = parseRem(reader);
                lhs = new BinaryOp(tk.pos, Tag.ADD, lhs, rhs);
                continue;
            }

            if (tk.hasType(MINUS)) {
                reader.step();
                Expr rhs = parseRem(reader);
                lhs = new BinaryOp(tk.pos, Tag.SUB, lhs, rhs);
                continue;
            }

            break;
        }

        return lhs;
    }

    private Expr parseRem(TokenReader reader) {
        // Look-ahead
        Expr lhs = parseMul(reader);

        while (reader.hasRemaining()) {
            Token tk = reader.currentToken();

            if (tk.hasType(PERCENT)) {
                reader.step();
                Expr rhs = parseMul(reader);
                lhs = new BinaryOp(tk.pos, Tag.REM, lhs, rhs);
                continue;
            }

            break;
        }

        return lhs;
    }

    private Expr parseMul(TokenReader reader) {
        // Look-ahead
        Expr lhs = parseUnary(reader);

        while (reader.hasRemaining()) {
            Token tk = reader.currentToken();

            if (tk.hasType(STAR)) {
                reader.step();
                Expr rhs = parseUnary(reader);
                lhs = new BinaryOp(tk.pos, Tag.MUL, lhs, rhs);
                continue;
            }

            if (tk.hasType(SLASH)) {
                reader.step();
                Expr rhs = parseUnary(reader);
                lhs = new BinaryOp(tk.pos, Tag.DIV, lhs, rhs);
                continue;
            }

            break;
        }

        return lhs;
    }

    private Expr parseUnary(TokenReader reader) {
        Token tk = reader.currentToken();

        if (tk.hasType(MINUS)) {
            reader.step();
            Expr expr = parseLowest(reader);
            return new UnaryOp(tk.pos, Tag.NEG, expr);
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
