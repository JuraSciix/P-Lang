package plang.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

    public static CharBuffer readCharBufferFromPath(Path path, Charset charset) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long size = channel.size();
            return charset.decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, size));
        }
    }
}
