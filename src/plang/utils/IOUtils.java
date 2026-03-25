package plang.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class IOUtils {

    public static String readFile(File file) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                output.write(buffer, 0, n);
            }
        }
        return output.toString();
    }
}
