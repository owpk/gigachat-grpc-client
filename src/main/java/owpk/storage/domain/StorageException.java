package owpk.storage.domain;

import java.io.IOException;

public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String path, IOException e) {
        super("Exception while processing file: " + path + "\n\t" + e.getLocalizedMessage());
    }
}
