package owpk.storage;

public abstract class AbstractStorage implements Storage {
    protected final String storageRoot;

    public AbstractStorage(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    protected String getFullPath(String relativePath) {
        return storageRoot + relativePath;
    }

}