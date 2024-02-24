package owpk.storage.jwt;

import owpk.storage.FileSettingsStore;
import owpk.storage.main.MainSettings;

import java.nio.file.Path;
import java.util.Properties;

// TODO separate jwt settings from main file
public class JwtSettingsStore implements FileSettingsStore<MainSettings.Jwt> {
    @Override
    public void init() {

    }

    @Override
    public Path getSettingsFile() {
        return null;
    }

    @Override
    public MainSettings.Jwt loadSettings() {
        return null;
    }

    @Override
    public MainSettings.Jwt getSettings() {
        return null;
    }

    @Override
    public Properties storeSettings(MainSettings.Jwt settings) {
        return null;
    }

    @Override
    public Properties createDefaults() {
        return null;
    }

}
