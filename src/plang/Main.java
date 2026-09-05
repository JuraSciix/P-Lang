package plang;

import plang.interpreter.BytecodeInterpreter;
import plang.interpreter.CodePrinter;
import plang.interpreter.ExecuteBlock;
import plang.interpreter.OPCodeList;
import plang.translator.*;
import plang.translator.codegen.Code;
import plang.translator.codegen.CodeData;
import plang.translator.codegen.CodeEmitter;
import plang.translator.codegen.Gen;
import plang.utils.IOUtils;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        Parser parser = new Parser();

        File file = new File(System.getProperty("user.dir"), "input/foo.pl");
        String str = IOUtils.readFile(file);

        LexResult lexResult = lexer.tokenize("test", str);
        ParserResult parserResult = parser.parse(lexResult);

//        AstPrintVisitor printVisitor = new AstPrintVisitor();
//        parserResult.getStatements().forEach(stmt -> stmt.accept(printVisitor));
//        printVisitor.flush();

        Code code = new Code();
        CodeEmitter emitter = new CodeEmitter();
        Gen gen = new Gen(code, emitter);
        parserResult.getStatements().forEach(stmt -> stmt.accept(gen));
        emitter.emit(OPCodeList.leave); // В конце всегда должна быть завершающая инструкция

        CodeData data = gen.getData();
        CodePrinter codePrinter = new CodePrinter();
        codePrinter.print(data.code, data.constantPool);


        BytecodeInterpreter interpreter = new BytecodeInterpreter();
        int result = interpreter.run(new ExecuteBlock(data.code, data.constantPool));
        System.out.println("Result: " + result);
    }
}