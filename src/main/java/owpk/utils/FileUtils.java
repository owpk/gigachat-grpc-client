package owpk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    /**
     * Returns true if file was created, false if it already exists
     */
    public static boolean createFileWithDirs(String str) {
        var path = Path.of(str);
        var parent = path.getParent();
        try {
            if (!Files.exists(parent)) {
                if (!Files.exists(Files.createDirectories(parent)))
                    throw new IllegalStateException("Couldn't create parent dirs: " + parent);
            }
            if (str.endsWith("/")) {
                if (!Files.exists(path)) {
                    if (!Files.exists(Files.createDirectory(path)))
                        throw new IllegalStateException("Couldn't create dir: " + path);
                    return true;
                }
            }
            else {
                if (!Files.exists(path)) {
                    if (!Files.exists(Files.createFile(path)))
                        throw new IllegalStateException("Couldn't create file: " + path);
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static Path createTempFile(byte[] data, String name) {
        Path path;
        try {
            path = Files.createTempFile(name, ".tmp");
            Files.write(path, data);
            return path;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
