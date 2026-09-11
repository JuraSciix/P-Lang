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

import static java.util.Arrays.stream;

public class Main {
    private static final int LEVEL_LEXER = 1;
    private static final int LEVEL_PARSER = 2;
    private static final int LEVEL_CODEGEN = 3;
    private static final int LEVEL_INTERPRETER = 4;

    private static final boolean PRINT_AST = false;
    private static final boolean PRINT_ASM = true;
    private static final int MEASURE_LEVEL = LEVEL_INTERPRETER;

    public static void main(String[] args) throws IOException {
        String name = "foo.pl";
        CharBuffer content = IOUtils.readCharBufferFromPath(
                Paths.get("input", name),
                StandardCharsets.UTF_8);
        measure(name, content, 2, MEASURE_LEVEL);
        Lexer lexer = new Lexer();
        LexResult lexResult = lexer.tokenize(name, content);
        Parser parser = new Parser();
        ParseResult parseResult = parser.parse(lexResult);
        if (PRINT_AST) {
            AstPrintVisitor printVisitor = new AstPrintVisitor();
            parseResult.getAst().accept(printVisitor);
            printVisitor.flush();
        }
        CodeData data = translate(parseResult);
        if (PRINT_ASM) {
            CodePrinter codePrinter = new CodePrinter();
            codePrinter.print(data.code, data.constantPool);
        }
        long result = run(data);
        System.out.println("Result: " + result);
    }

    private static CodeData translate(ParseResult parseResult) {
        // Все аллокации после выхода из метода должны освободиться
        CodeEmitter emitter = new CodeEmitter();
        Code code = new Code();
        Gen gen = new Gen(code);
        Items.Item resultItem = gen.gen(parseResult.getAst());
        if (resultItem.alive()) {
            // Добавляем return
            gen.gen(new Ast.Return(0, null));
        }
        return code.toData();
    }

    private static long run(CodeData data) {
        long[] memoryData = new long[256];
        BytecodeInterpreter interpreter = new BytecodeInterpreter();
        int result = interpreter.run(data.code, data.constantPool, 0, memoryData, 0, 10);
        return memoryData[result];
    }

    private static void measure(String name, CharBuffer content, int rep, int level) {
        if (level < LEVEL_LEXER) return;
        long[] lexMeasures = new long[rep];
        long[] parseMeasures = new long[rep];
        long[] genMeasures = new long[rep];
        long[] runMeasures = new long[rep];

        for (int i = 0; i < rep; i++) {
            Lexer lexer = new Lexer();
            long lexTx = System.nanoTime();
            LexResult lexResult = lexer.tokenize(name, content);
            long lexTy = System.nanoTime();
            lexMeasures[i] = lexTy - lexTx;

            ParseResult parseResult = null;
            if (level >= LEVEL_PARSER) {
                Parser parser = new Parser();
                long parseTx = System.nanoTime();
                parseResult = parser.parse(lexResult);
                long parseTy = System.nanoTime();
                parseMeasures[i] = parseTy - parseTx;
            }

            CodeData data = null;
            if (level >= LEVEL_CODEGEN) {
                Code code = new Code();
                Gen gen = new Gen(code);
                long genTx = System.nanoTime();
                Items.Item item = gen.gen(parseResult.getAst());
                if (item.alive()) {
                    gen.gen(new Ast.Return(0, null));
                }
                data = code.toData();
                long genTy = System.nanoTime();
                genMeasures[i] = genTy - genTx;
            }

            if (level >= LEVEL_INTERPRETER) {
                long[] memoryData = new long[256];
                BytecodeInterpreter interpreter = new BytecodeInterpreter();
                long runTx = System.nanoTime();
                interpreter.run(data.code, data.constantPool, 0, memoryData, 0, 10);
                long runTy = System.nanoTime();
                runMeasures[i] = runTy - runTx;
            }
        }

        printMeasures("Lexing", lexMeasures);
        if (level >= LEVEL_PARSER) printMeasures("Parsing", parseMeasures);
        if (level >= LEVEL_CODEGEN) printMeasures("Codegen", genMeasures);
        if (level >= LEVEL_INTERPRETER) printMeasures("Run", runMeasures);
    }

    private static void printMeasures(String title, long[] measures) {
        long best = stream(measures).min().orElse(0);
        long worth = stream(measures).max().orElse(0);
        System.out.printf("%-12s %-16s %-16s %n",
                title,
                "top " + best / 1000 + " hs",
                "bot " + worth / 1000 + " hs");
    }
}