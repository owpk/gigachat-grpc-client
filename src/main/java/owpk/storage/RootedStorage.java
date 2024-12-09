package owpk.storage;

import java.util.List;

import lombok.Getter;
import owpk.storage.domain.Content;

@Getter
public class RootedStorage implements Storage {
    protected final String storageRoot;
    protected final Storage delegate;

    public RootedStorage(String storageRoot, Storage delegate) {
        this.storageRoot = storageRoot;
        this.delegate = delegate;
    }

    @Override
    public byte[] getContent(String path) {
        return delegate.getContent(concatePath(path));
    }

    @Override
    public List<Content> getContents(String path) {
        return delegate.getContents(concatePath(path));
    }

    @Override
    public boolean saveContent(String path, byte[] content, boolean append) {
        return delegate.saveContent(concatePath(path), content, append);
    }

    @Override
    public void saveContents(List<Content> contents) {
        var mapped = contents.stream()
            .map(it -> new Content(it.data(), concatePath(it.filename())))
            .toList();
        delegate.saveContents(mapped);
    }

    @Override
    public boolean exists(String path) {
        return delegate.exists(concatePath(path));
    }

    @Override
    public String createFileOrDir(String path) {
        return delegate.createFileOrDir(concatePath(path));
    }

    private String concatePath(String path) {
        return String.format("%s/%s", storageRoot, path);
    }

}