package plang;

import plang.interpreter.BytecodeInterpreter;
import plang.interpreter.CodePrinter;
import plang.translator.*;
import plang.translator.codegen.*;
import plang.utils.IOUtils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class Main {
    private static final boolean PRINT_AST = true;
    private static final boolean PRINT_ASM = true;
    private static final boolean MEASURE_LEXER = false;
    private static final boolean MEASURE_PARSER = false;
    private static final boolean MEASURE_CODEGEN = false;
    private static final boolean MEASURE_INTERPRETER = false;

    public static void main(String[] args) throws IOException {
        String name = "foo.pl";
        CharBuffer content = IOUtils.readCharBufferFromPath(
                Paths.get("input", name),
                StandardCharsets.UTF_8);
        if (MEASURE_LEXER) {
            measureLexer(name, content);
        }

        Lexer lexer = new Lexer();
        LexResult lexResult = lexer.tokenize(name, content);
        if (MEASURE_PARSER) {
            measureParser(lexResult);
        }

        Parser parser = new Parser();
        ParseResult parseResult = parser.parse(lexResult);
        if (PRINT_AST) {
            AstPrintVisitor printVisitor = new AstPrintVisitor();
            parseResult.getAst().accept(printVisitor);
            printVisitor.flush();
        }
        if (MEASURE_CODEGEN) {
            measureGen(parseResult);
        }

        CodeData data = translate(parseResult);
        if (PRINT_ASM) {
            CodePrinter codePrinter = new CodePrinter();
            codePrinter.print(data.code, data.constantPool);
        }
        if (MEASURE_INTERPRETER) {
            measureRun(data);
        }

        long result = run(data);
        System.out.println("Result: " + result);
    }

    private static CodeData translate(ParseResult parseResult) {
        // Все аллокации после выхода из метода должны освободиться
        Code code = new Code();
        CodeEmitter emitter = new CodeEmitter();
        Gen gen = new Gen(code, emitter);
        Items.Item resultItem = gen.gen(parseResult.getAst());
        if (resultItem.alive()) {
            // Добавляем return
            gen.gen(new Ast.Return(0, null));
        }
        return gen.getData();
    }

    private static long run(CodeData data) {
        long[] memoryData = new long[256];
        BytecodeInterpreter interpreter = new BytecodeInterpreter();
        int result = interpreter.run(data.code, data.constantPool, 0, memoryData, 0, 10);
        return memoryData[result];
    }

    private static void measureLexer(String name, CharBuffer content) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        Lexer lexer = new Lexer();
        for (int i = 0; i < 10; i++) {
            long tx = System.nanoTime();
            for (int j = 0; j < 10; j++) {
                lexer.tokenize(name, content);
            }
            long ty = System.nanoTime();
            long dt = (ty - tx) / (10 * 1000);
            max = Math.max(max, dt);
            min = Math.min(min, dt);
        }
        System.out.println("Lexing measurement: " + max + " - " + min + " mcs");
    }

    private static void measureParser(LexResult lexResult) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        Parser parser = new Parser();
        for (int i = 0; i < 10; i++) {
            long tx = System.nanoTime();
            for (int j = 0; j < 10; j++) {
                parser.parse(lexResult);
            }
            long ty = System.nanoTime();
            long dt = (ty - tx) / (10 * 1000);
            max = Math.max(max, dt);
            min = Math.min(min, dt);
        }
        System.out.println("Parsing measurement: " + max + " - " + min + " mcs");
    }

    private static void measureGen(ParseResult parseResult) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for (int i = 0; i < 10; i++) {
            Code code = new Code();
            CodeEmitter emitter = new CodeEmitter();
            Gen gen = new Gen(code, emitter);
            long tx = System.nanoTime();
            for (int j = 0; j < 5; j++) {
                parseResult.getAst().accept(gen);
            }
            long ty = System.nanoTime();
            long dt = (ty - tx) / (5 * 1000);
            max = Math.max(max, dt);
            min = Math.min(min, dt);
        }
        System.out.println("Generating measurement: " + max + " - " + min + " mcs");
    }

    private static void measureRun(CodeData data) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;

        long[] memoryData = new long[256];
        BytecodeInterpreter interpreter = new BytecodeInterpreter();
        for (int i = 0; i < 10; i++) {
            long tx = System.nanoTime();
            interpreter.run(data.code, data.constantPool, 0, memoryData, 0, 10);
            long ty = System.nanoTime();
            long dt = (ty - tx) / 1000;
            max = Math.max(max, dt);
            min = Math.min(min, dt);
        }
        System.out.println("Executed measurement: " + max + " - " + min + " mcs");
    }
}