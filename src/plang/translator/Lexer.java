package plang.translator;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static plang.translator.TokenType.*;

public class Lexer {
    private static boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }

    private static boolean isAlpha(int ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z';
    }

    private static boolean isSpecial1(int ch) {
        return ch == '$' || ch == '_';
    }

    private static boolean isSpecial2(int ch) {
        switch (ch) {
            case '+': case '-': case '*': case '/': case '%':
            case '&': case '|': case '^': case '~':
            case '<': case '>': case '=': case '!':
                return true;
            default:
                return false;
        }
    }

    private final Map<CharSequence, TokenType> keywordMap = new HashMap<>();

    public Lexer() {
        keywordMap.put("if", IF);
        keywordMap.put("else", ELSE);
        keywordMap.put("while", WHILE);
        keywordMap.put("return", RETURN);
    }

    public LexResult tokenize(String sourceName, CharBuffer content) {
        StringReader reader = new StringReader(content);

        List<Token> tokens = new ArrayList<>();

        while (reader.hasRemaining()) {
            int pos = reader.getPosition();

            switch (reader.currentChar()) {
                case ' ': case '\t': case '\r':
                    reader.step();
                    break;

                case '\n':
                    reader.step();
                    tokens.add(new Token(pos, pos + 1, SEP));
                    break;

                // Межстрочное соединение
                case '\\':
                    afterBackslash(reader);
                    break;

                // Однострочный комментарий
                case '#':
                    afterGrid(reader);
                    break;

                case '0':
                    // Числа с нуля (кроме самого нуля) начинаться не могут.
                    reader.step();
                    tokens.add(new Token(pos, pos + 1, INTEGER));
                    break;

                case '(':
                    reader.step();
                    tokens.add(new Token(pos, pos + 1, LPAREN));
                    break;

                case ')':
                    reader.step();
                    tokens.add(new Token(pos, pos + 1, RPAREN));
                    break;

                case '{':
                    reader.step();
                    tokens.add(new Token(pos, pos + 1, LBRACE));
                    break;

                case '}':
                    reader.step();
                    tokens.add(new Token(pos, pos + 1, RBRACE));
                    break;

                default:
                    int ch = reader.currentChar();
                    if (isDigit(ch)) {
                        afterDigit(reader);
                        int endPos = reader.getPosition();
                        tokens.add(new Token(pos, endPos, INTEGER));
                    } else if (isAlpha(ch) || isSpecial1(ch)) {
                        afterIdentifierPart(reader);
                        int endPos = reader.getPosition();
                        CharSequence csq = reader.subseq(pos, endPos).toString();
                        tokens.add(new Token(pos, endPos, keywordMap.getOrDefault(csq, IDENTIFIER)));
                    } else if (isSpecial2(ch)) {
                        TokenType type = afterSpecial2(reader);
                        int endPos = reader.getPosition();
                        tokens.add(new Token(pos, endPos, type));
                    } else {
                        throw new IllegalArgumentException(
                                String.format("Illegal character U+%04x", ch));
                    }
            }
        }

        LineNumberMap lineNumberMap = new LineNumberMap(new int[0]); // todo
        return new LexResult(sourceName, content, lineNumberMap, tokens);
    }

    private void afterBackslash(StringReader reader) {
        // Текущий символ: обратный слеш
        reader.step();

        // После обратного слеша ожидаем увидеть только
        // пробелы/табуляцию/перенос строки/перенос каретки.

        boolean seeking = true;
        while (reader.hasRemaining() && seeking) {
            switch (reader.currentChar()) {
                case '\n':
                    seeking = false;
                case ' ':
                case '\t':
                case '\r':
                    reader.step();
                    break;
                default:
                    throw new RuntimeException("Unexpected char after backslash");
            }
        }
    }

    private void afterGrid(StringReader reader) {
        // Текущий символ: решетка
        reader.step();

        while (reader.hasRemaining()) {
            if (reader.matches('\n')) {
                break;
            }
            reader.step();
        }
    }

    private TokenType afterSpecial2(StringReader reader) {
        char ch = reader.currentChar();
        reader.step();

        switch (ch) {
            case '+': return PLUS;
            case '-': return MINUS;
            case '*': return STAR;
            case '/': return SLASH;
            case '%': return PERCENT;
            case '&': return reader.matches('&') ? AMPAMP : AMP;
            case '|': return reader.matches('|') ? BARBAR : BAR;
            case '!': return reader.matches('=') ? NOT_EQ : BANG;
            case '=': return reader.matches('=') ? EQ : ASG;
            case '>': return reader.matches('=') ? GT_EQ : GT;
            case '<': return reader.matches('=') ? LT_EQ : LT;
            default: throw new AssertionError(ch);
        }
    }

    private void afterDigit(StringReader reader) {
        reader.step();

        while (reader.hasRemaining()) {
            char ch = reader.currentChar();
            if (ch == '_') {
                reader.step();
                if (reader.hasRemaining() && isDigit(reader.currentChar())) {
                    // Допускаем нижнее подчеркивание перед цифрой.
                    continue;
                }
                throw new IllegalArgumentException("Underscore is not allowed here");
            }
            if (isDigit(ch)) {
                reader.step();
            } else {
                break;
            }
        }
    }

    private void afterIdentifierPart(StringReader reader) {
        reader.step();

        while (reader.hasRemaining()) {
            char ch = reader.currentChar();
            if (isAlpha(ch) || isSpecial1(ch) || isDigit(ch)) {
                reader.step();
            } else {
                break;
            }
        }
    }
}
