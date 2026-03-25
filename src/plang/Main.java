package plang;

import plang.interpreter.CodePrinter;
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
        ParserResult parserResult = parser.parse(lexerResult);

        Code code = new Code();
        Gen gen = new Gen(code);
        parserResult.getStatements().forEach(stmt -> stmt.accept(gen));

        CodePrinter codePrinter = new CodePrinter();
        codePrinter.print(code.getCode());
    }
}