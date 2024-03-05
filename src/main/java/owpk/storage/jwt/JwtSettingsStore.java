package owpk.storage.jwt;

import owpk.storage.AbsPropertiesFileStorage;
import owpk.storage.FileSettingsStore;
import owpk.storage.main.MainSettings;

import java.nio.file.Path;
import java.util.Properties;

// TODO separate jwt settings from main file
public class JwtSettingsStore extends AbsPropertiesFileStorage<MainSettings.Jwt> {
    @Override
    protected MainSettings.Jwt initSettings() {
        return null;
    }

    @Override
    protected Path initSettingsFile() {
        return null;
    }

    @Override
    public Properties storeSettings(MainSettings.Jwt settings) {
        return null;
    }

    @Override
    public void createDefaults() {
    }
}
