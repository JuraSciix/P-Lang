package plang;

import plang.interpreter.BytecodeInterpreter;
import plang.interpreter.CodePrinter;
import plang.interpreter.ExecuteBlock;
import plang.interpreter.OPCodeList;
import plang.translator.LexResult;
import plang.translator.Lexer;
import plang.translator.Parser;
import plang.translator.ParserResult;
import plang.translator.codegen.Code;
import plang.translator.codegen.CodeData;
import plang.translator.codegen.CodeEmitter;
import plang.translator.codegen.Gen;
import plang.utils.IOUtils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("input", "foo.pl");
        CharBuffer content = IOUtils.readCharBufferFromPath(path, StandardCharsets.UTF_8);

        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        LexResult lexResult = lexer.tokenize("test", content);
        ParserResult parserResult = parser.parse(lexResult);

//        AstPrintVisitor printVisitor = new AstPrintVisitor();
//        parserResult.getStatements().forEach(stmt -> stmt.accept(printVisitor));
//        printVisitor.flush();

        Code code = new Code();
        CodeEmitter emitter = new CodeEmitter();
        Gen gen = new Gen(code, emitter);
        parserResult.getAst().accept(gen);

        CodeData data = gen.getData();
        CodePrinter codePrinter = new CodePrinter();
        codePrinter.print(data.code, data.constantPool);

        int n = 1;
        for (int i = 0; i < n; i++) {
            long tx = System.nanoTime();
            long result = BytecodeInterpreter.run(new ExecuteBlock(data.code, data.constantPool));
            long ty = System.nanoTime();

            long hs = (ty - tx) / 1000;
            System.out.println("Executed in " + hs + " hs. Result: " + result);
        }
    }
}