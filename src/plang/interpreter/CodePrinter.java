package plang.interpreter;

import java.io.PrintStream;

public class CodePrinter {
    private final PrintStream stream;

    public CodePrinter() {
        this(System.out);
    }

    public CodePrinter(PrintStream stream) {
        this.stream = stream;
    }

    public void print(byte[] code, int[] constantPool) {
        int i = 0;
        while (i < code.length) {
            OPCodeInfo info = OPCodeInfo.info(Bytes.readUB(code, i));
            stream.printf("%4d. ", i);
            i++;
            stream.print(info.name);
            for (OPCodeInfo.Param param : info.params) {
                stream.print(' ');
                switch (param) {
                    case REG:
                        stream.print('$');
                        stream.print(Bytes.readUB(code, i));
                        i++;
                        break;
                    case CONST_ID:
                        int constIndex = Bytes.read2UB(code, i);
                        stream.print('%');
                        stream.print(constantPool[constIndex]);
                        i += 2;
                        break;
                    case BCI:
                        stream.print('.');
                        stream.print(Bytes.read2UB(code, i));
                        i += 2;
                        break;
                }
            }
            stream.println();
        }
    }
}
