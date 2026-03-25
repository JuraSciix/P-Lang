package plang;

import plang.translator.*;
import plang.utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        File file = new File(System.getProperty("user.dir"), "input/foo.txt");
        String str = IOUtils.readFile(file);

        LexerResult lexerResult = lexer.tokenize("test", str);
        lexerResult.getTokens().forEach(System.out::println);
        System.out.println("Tokens: " + lexerResult.getTokens().size());
        ParserResult parserResult = parser.parse(lexerResult);

        AstPrintVisitor visitor = new AstPrintVisitor();
        parserResult.getStatements().forEach(stmt -> stmt.accept(visitor));
        visitor.flush();
    }
}