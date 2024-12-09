package owpk.storage.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import owpk.storage.Storage;
import owpk.storage.domain.Content;
import owpk.storage.domain.StorageException;
import owpk.utils.FileUtils;

public class LocalFileStorage implements Storage {

    @Override
    public byte[] getContent(String path) {
        try {
            return Files.readAllBytes(Path.of(path));
        } catch (IOException e) {
            throw new StorageException(path, e);
        }
    }

    @Override
    public List<Content> getContents(String path) {
        try {
            var target = Path.of(path);
            if (Files.isDirectory(target)) {
                return Files.walk(target)
                    .filter(Files::isRegularFile)
                    .map(p -> {
                        try {
                            return new Content(Files.readAllBytes(p), p.toString());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .toList();
            } else if (Files.isRegularFile(target)) {
                return List.of(new Content(Files.readAllBytes(target), target.toString()));
            } else {
                throw new StorageException("Path is neither a file nor a directory: " + path);
            }
        } catch (IOException e) {
            throw new StorageException(path, e);
        }
    }

    @Override
    public boolean saveContent(String path, byte[] content, boolean append) {
        try {
            var normalized = Path.of(path).normalize();
            var createdFile = Files.write(normalized, content, append ?
                StandardOpenOption.APPEND :
                StandardOpenOption.TRUNCATE_EXISTING);
            return Files.exists(createdFile);
        } catch (IOException e) {
            throw new StorageException(path, e);
        }
    }

    @Override
    public void saveContents(List<Content> contents) {
        contents.forEach(it -> saveContent(it.filename(), it.data(), false));
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(Path.of(path));
    }

    @Override
    public String createFileOrDirIfNotExists(String path) {
        return FileUtils.createFileWithDirs(path) ? path : null;
    }
    
}
