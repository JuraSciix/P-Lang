package plang.translator;

import plang.utils.IntArrayList;
import plang.utils.Trie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static plang.translator.TokenType.*;
import static plang.translator.TokenType.BANG;
import static plang.translator.TokenType.IDENTIFIER;
import static plang.translator.TokenType.INTEGER;
import static plang.translator.TokenType.MINUS;
import static plang.translator.TokenType.PLUS;

public class Lexer {
    private final Trie<TokenType> operators = new Trie<>();
    private final Map<String, TokenType> keywordMap = new HashMap<>();

    public Lexer() {
        operators.put("+", PLUS);
        operators.put("-", MINUS);
        operators.put("*", STAR);
        operators.put("/", SLASH);
        operators.put("%", PERCENT);
        operators.put("(", LPAREN);
        operators.put(")", RPAREN);
        operators.put("{", LBRACE);
        operators.put("}", RBRACE);
        operators.put("=", ASG);
        operators.put("==", EQ);
        operators.put("!=", NOT_EQ);
        operators.put("<", LT);
        operators.put("<=", LT_EQ);
        operators.put(">", GT);
        operators.put(">=", GT_EQ);
        operators.put(";", SEP);

        keywordMap.put("if", IF);
        keywordMap.put("else", ELSE);
        keywordMap.put("while", WHILE);
        keywordMap.put("return", RETURN);
    }

    public LexResult tokenize(String sourceName, String str) {
        StringReader reader = new StringReader(str);
        StringBuilder buffer = new StringBuilder();

        List<Token> tokens = new ArrayList<>();
        List<TokenData> tokensData = new ArrayList<>();
        IntArrayList lineStartPositions = new IntArrayList();

        while (reader.hasRemaining()) {
            int pos = reader.getPosition();
            char ch = reader.currentChar();

            if (ch == '\n') {
                reader.step();
                lineStartPositions.add(reader.getPosition());
                tokens.add(new Token(pos, TokenType.SEP));
                continue;
            }

            if (ch == ' ' || ch == '\t' || ch == '\r') {
                reader.step();
                continue;
            }

            if (ch == '\\') {
                // Перенос строки: \\ + \r?\n
                reader.step();
                if (reader.hasRemaining()) {
                    char ch1 = reader.currentChar();
                    boolean newline = false;
                    if (ch1 == '\n') {
                        reader.step();
                        newline = true;
                    }
                    if (ch1 == '\r') {
                        reader.step();
                        if (reader.hasRemaining()) {
                            ch1 = reader.currentChar();
                            if (ch1 == '\n') {
                                reader.step();
                                newline = true;
                            }
                        }
                    }
                    if (newline) {
                        continue;
                    }
                }

                reader.stepBack();
            }

            // Особый случай
            if (ch == '/') {
                reader.step();
                if (reader.hasRemaining()) {
                    char ch1 = reader.currentChar();
                    if (ch1 == '/') {
                        reader.step();
                        while (reader.hasRemaining()) {
                            ch1 = reader.currentChar();
                            reader.step();
                            if (ch1 == '\n' || ch1 == '\r') {
                                break;
                            }
                        }
                        continue;
                    }

                    if (ch1 == '*') {
                        // Ищем закрывающий тег
                        boolean pending = true;
                        reader.step();
                        while (reader.hasRemaining()) {
                            ch1 = reader.currentChar();
                            reader.step();
                            if (ch1 == '*' && reader.hasRemaining()) {
                                ch1 = reader.currentChar();
                                reader.step();
                                if (ch1 == '/') {
                                    pending = false;
                                    break;
                                }
                            }
                        }

                        if (pending) {
                            throw new IllegalArgumentException();
                        }
                        continue;
                    }
                }

                // Возвращаемся назад!
                reader.stepBack();
            }

            if (ch == '!') {
                reader.step();
                TokenType type = BANG;
                if (reader.matches('=')) {
                    type = NOT_EQ;
                }
                tokens.add(new Token(pos, type));
                continue;
            }

            if (ch == '0') {
                // Числа с нуля (кроме самого нуля) начинаться не могут.
                reader.step();
                tokens.add(new Token(pos, INTEGER));
                tokensData.add(new TokenData(pos, "0"));
                continue;
            }

            if ('1' <= ch && ch <= '9') {
                buffer.append(ch);
                reader.step();
                while (reader.hasRemaining()) {
                    ch = reader.currentChar();
                    if (ch == '_') {
                        reader.step();
                        if (reader.hasRemaining()) {
                            char ch1 = reader.currentChar();
                            if ('0' <= ch1 && ch1 <= '9') {
                                continue;
                            }
                        }
                        throw new IllegalArgumentException("Digit expected at " + reader.getPosition());
                    }
                    if (!('0' <= ch && ch <= '9')) {
                        break;
                    }
                    buffer.append(ch);
                    reader.step();
                }
                String data = buffer.toString();
                buffer.setLength(0);
                tokens.add(new Token(pos, INTEGER));
                tokensData.add(new TokenData(pos, data));
                continue;
            }

            Trie.Node<TokenType> operatorNode = operators.findNode(ch);
            if (operatorNode != null) {
                Trie.Node<TokenType> n = operatorNode;
                reader.step();
                while (reader.hasRemaining()) {
                    ch = reader.currentChar();
                    operatorNode = n;
                    n = n.findChild(ch);
                    if (n == null) {
                        break;
                    }
                    reader.step();
                }

                TokenType type = operatorNode.getValue();
                tokens.add(new Token(pos, type));
                continue;
            }

            if (Character.isJavaIdentifierStart(ch)) {
                buffer.append(ch);
                reader.step();
                while (reader.hasRemaining()) {
                    ch = reader.currentChar();
                    if (!Character.isJavaIdentifierPart(ch)) {
                        break;
                    }
                    buffer.append(ch);
                    reader.step();
                }

                String data = buffer.toString();
                buffer.setLength(0);

                if (keywordMap.containsKey(data)) {
                    TokenType type = keywordMap.get(data);
                    tokens.add(new Token(pos, type));
                } else {
                    tokens.add(new Token(pos, IDENTIFIER));
                    tokensData.add(new TokenData(pos, data));
                }

                continue;
            }

            int codePoint = ch;
            throw new IllegalArgumentException(String.format("Illegal character U+%04x", codePoint));
        }

        LineNumberMap lineNumberMap = new LineNumberMap(lineStartPositions.toArray());
        return new LexResult(sourceName, lineNumberMap, tokens, tokensData);
    }
}
