package plang.interpreter;

import java.io.PrintStream;

public class CodePrinter {

    private final PrintStream stream = System.out;

    public void print(byte[] code) {
        int i = 0;
        while (i < code.length) {
            int w = OPCodeInfo.weight(code[i]);
            stream.printf("%4d. ", i);
            stream.print(OPCodeInfo.opcodeString(code[i]));
            for (int j = 1; j < w; j++) {
                stream.print(' ');
                stream.print(code[i + j]);
            }
            stream.println();
            i += w;
        }
    }
}
