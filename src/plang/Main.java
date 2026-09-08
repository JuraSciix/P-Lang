package plang;

import plang.interpreter.BytecodeInterpreter;
import plang.interpreter.CodePrinter;
import plang.translator.*;
import plang.translator.codegen.*;
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
        ParseResult parseResult = parser.parse(lexResult);

//        AstPrintVisitor printVisitor = new AstPrintVisitor();
//        parserResult.getStatements().forEach(stmt -> stmt.accept(printVisitor));
//        printVisitor.flush();

        Code code = new Code();
        CodeEmitter emitter = new CodeEmitter();
        Gen gen = new Gen(code, emitter);
        long ctx = System.nanoTime();
        Items.Item resultItem = gen.gen(parseResult.getAst());
        if (resultItem.alive()) {
            // Добавляем return
            gen.gen(new Ast.Return(0, null));
        }
        long cty = System.nanoTime();
        long ctm = (cty - ctx) / 1000;

        CodeData data = gen.getData();
        CodePrinter codePrinter = new CodePrinter();
        codePrinter.print(data.code, data.constantPool);
        System.out.println("Compiled in " + ctm + " hs. Instructions: " + data.code.length);

        long[] memoryData = new long[256];
        BytecodeInterpreter interpreter = new BytecodeInterpreter();

        int n = 1;
        for (int i = 0; i < n; i++) {
            long etx = System.nanoTime();
            int result = interpreter.run(data.code, data.constantPool, 0, memoryData, 0, 10);
            long ety = System.nanoTime();

            long etm = (ety - etx) / 1000;
            System.out.println("Executed in " + etm + " hs. Result: " + memoryData[result]);
        }
    }
}