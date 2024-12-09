package owpk.storage;

import java.util.List;

import owpk.storage.domain.Content;
import owpk.storage.domain.StorageException;

public interface Storage {
    byte[] getContent(String path) throws StorageException;
    List<Content> getContents(String path) throws StorageException;
    void saveContents(List<Content> contents) throws StorageException;
    boolean exists(String path);
    String createFileOrDirIfNotExists(String path);
    boolean saveContent(String path, byte[] composedArray, boolean append) throws StorageException;
}
