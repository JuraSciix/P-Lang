package plang.utils;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IOUtils {

    public static CharBuffer readCharBufferFromPath(Path path, Charset charset) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long size = channel.size();
            return charset.decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, size));
        }
    }
}
