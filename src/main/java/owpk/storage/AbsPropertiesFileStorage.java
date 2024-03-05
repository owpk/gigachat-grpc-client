package owpk.storage;

import lombok.extern.slf4j.Slf4j;
import owpk.Application;
import owpk.utils.FileUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public abstract class AbsPropertiesFileStorage<T> implements FileSettingsStore<T> {
    protected Path settingsFile;
    protected Properties properties;
    protected T settings;

    public AbsPropertiesFileStorage() {
        properties = new Properties();
        settingsFile = initSettingsFile();

        load();
        settings = initSettings();
        log.info("Init application settings store: " + settingsFile);
    }

    protected abstract T initSettings();

    protected abstract Path initSettingsFile();

    protected void load() {
        if (Files.exists(settingsFile)) {
            try (var fis = new FileInputStream(settingsFile.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Path getSettingsFile() {
        return settingsFile;
    }

    @Override
    public T getSettings() {
        return settings;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    protected Path defaultInit(Path target) {
        if (FileUtils.createFileWithDirs(target))
            throw new RuntimeException("Can't create file: " + target);
        return target;
    }

    protected Path defaultInit(String fromAppHome) {
        var target = Paths.get(Application.APP_HOME_DIR.toString(), fromAppHome);
        if (FileUtils.createFileWithDirs(target)) {
            this.settingsFile = target;
            createDefaults();
        }
        return target;
    }

    protected void storeProps() {
        log.info("Storing properties...");
        FileSettingsStore.storeProps(properties, settingsFile);
    }

}