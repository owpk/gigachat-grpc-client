package owpk.storage.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public interface FileSettingsStore<T> {
    static void storeProps(Properties properties, Path settingsFile) {
        try (var fos = new FileOutputStream(settingsFile.toFile())) {
            properties.store(fos, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Path getSettingsFile();

    T getSettings();

    Properties storeSettings(T settings);

    void createDefaults();

    Properties getProperties();

    void validate(Runnable missingBasicCredentialsPrinter);
}
