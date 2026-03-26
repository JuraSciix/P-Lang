package plang.interpreter;

import java.io.PrintStream;

public class CodePrinter {

    private final PrintStream stream = System.out;

    public void print(byte[] code) {
        int i = 0;
        while (i < code.length) {
            OPCodeInfo info = OPCodeInfo.info(code[i++]);
            stream.printf("%4d. ", i);
            stream.print(info.name);
            for (OPCodeInfo.Param param : info.params) {
                stream.print(' ');
                switch (param) {
                    case INDEX:
                        stream.print(code[i]);
                        i++;
                        break;
                    case DOUBLE_INDEX:
                        stream.print(Bytes.read2ub(code, i));
                        i += 2;
                        break;
                }
            }
            stream.println();
        }
    }
}
