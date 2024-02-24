package owpk.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    /**
     * Returns true if file was created, false if it already exists
     */
    public static boolean createFileWithDirs(Path path) {
        var parent = path.getParent();
        try {
            if (!Files.exists(parent) && !Files.exists(Files.createDirectories(parent)))
                throw new IllegalStateException("Couldn't create dir: " + path);
            if (!Files.exists(path)) {
                if (!Files.exists(Files.createFile(path)))
                    throw new IllegalStateException("Couldn't create file: " + path);
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
