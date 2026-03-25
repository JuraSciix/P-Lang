package plang.translator;

import plang.utils.IntArrayBuilder;
import plang.utils.Trie;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final Trie<TokenType> operators = new Trie<>();
    private final Trie<TokenType> keywords = new Trie<>();

    private final StringBuilder buffer = new StringBuilder();

    public Lexer() {
        operators.put("+", TokenType.PLUS);
        operators.put("-", TokenType.MINUS);
        operators.put("*", TokenType.STAR);
        operators.put("/", TokenType.SLASH);
        operators.put("%", TokenType.PERCENT);
        operators.put("(", TokenType.LPAREN);
        operators.put(")", TokenType.RPAREN);
        operators.put("{", TokenType.LBRACE);
        operators.put("}", TokenType.RBRACE);
        operators.put("=", TokenType.ASG);
        operators.put("==", TokenType.EQ);
        operators.put(";", TokenType.SEP);

        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("return", TokenType.RETURN);
    }

    public LexerResult tokenize(String sourceName, String str) {
        StringReader reader = new StringReader(str);

        List<Token> tokens = new ArrayList<>();
        List<TokenData> tokensData = new ArrayList<>();
        IntArrayBuilder lineStartPositions = new IntArrayBuilder();

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

            if (ch == '0') {
                // Числа с нуля (кроме самого нуля) начинаться не могут.
                reader.step();
                tokens.add(new Token(pos, TokenType.INTEGER));
                tokensData.add(new TokenData(pos, "0"));
                continue;
            }

            if ('1' <= ch && ch <= '9') {
                buffer.append(ch);
                reader.step();
                while (reader.hasRemaining()) {
                    ch = reader.currentChar();
                    if (!('0' <= ch && ch <= '9')) {
                        break;
                    }
                    buffer.append(ch);
                    reader.step();
                }
                String data = buffer.toString();
                buffer.setLength(0);
                tokens.add(new Token(pos, TokenType.INTEGER));
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

                if (keywords.contains(data)) {
                    TokenType type = keywords.get(data);
                    tokens.add(new Token(pos, type));
                } else {
                    tokens.add(new Token(pos, TokenType.IDENTIFIER));
                    tokensData.add(new TokenData(pos, data));
                }

                continue;
            }

            int codePoint = ch;
            throw new IllegalArgumentException(String.format("Illegal character U+%04x", codePoint));
        }

        LineNumberMap lineNumberMap = new LineNumberMap(lineStartPositions.toArray());
        return new LexerResult(sourceName, lineNumberMap, tokens, tokensData);
    }
}
