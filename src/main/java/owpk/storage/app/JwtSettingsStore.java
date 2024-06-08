package owpk.storage.app;

import owpk.settings.main.MainSettings;

import java.util.Properties;

// TODO separate jwt settings from main file
public class JwtSettingsStore extends AbsPropertiesFileStorage<MainSettings.Jwt> {
    @Override
    protected MainSettings.Jwt initSettings() {
        return null;
    }

    @Override
    protected void initSettingsFile() {
    }

    @Override
    public Properties storeSettings(MainSettings.Jwt settings) {
        return null;
    }

    @Override
    public void createDefaults() {
    }

    @Override
    public void validate(Runnable missingBasicCredentialsPrinter) {

    }
}
