package owpk.storage;

import java.nio.file.Path;

public interface Storage {

    Path createFile(String path);

    byte[] readFile(String path);

    void deleteFile(String path);

    void writeFile(String path, byte[] content, boolean append);

    boolean exists(String path);
}
