package owpk.storage;

import owpk.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class LocalStorage extends AbstractStorage {

    public LocalStorage(String storageRoot) {
        super(storageRoot);
    }

    public LocalStorage(Path path) {
        super(path.toString());
    }

    @Override
    public Path createFile(String path) {
        var target = Path.of(this.storageRoot, path);
        var created = FileUtils.createFileWithDirs(target);
        return target;
    }

    @Override
    public byte[] readFile(String path) {
        var target = Path.of(this.storageRoot, path);
        try {
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String path) {
        var target = Path.of(this.storageRoot, path);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFile(String path, byte[] content, boolean append) {
        var target = createFile(path);
        try {
            Files.write(target, content, append ?
                    StandardOpenOption.APPEND :
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String path) {
        var target = Path.of(this.storageRoot, path);
        return Files.exists(target);
    }
}
