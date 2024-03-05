package owpk.storage.main;

import lombok.extern.slf4j.Slf4j;
import owpk.Application;
import owpk.storage.AbsPropertiesFileStorage;
import owpk.storage.FileSettingsStore;

import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

@Slf4j
public class MainSettingsStore extends AbsPropertiesFileStorage<MainSettings> {

    public static Properties getDefaltProperties() {
        log.info("Creating default application store properties...");
        var props = new Properties();
        props.putAll(MainSettingField.propertiesMap);
        return props;
    }

    public void validate(Runnable missingBasicCredentialsPrinter) {
        log.info("Validating application settings store properties...");
        load();
        if (getProperty(MainSettingField.COMPOSED_CREDENTIALS.getPropertyKey()).isBlank()) {
            missingBasicCredentialsPrinter.run();
            var input = new Scanner(System.in);
            var credentials = input.nextLine();
            if (credentials == null || credentials.isBlank())
                throw new Error("No credentials specified");
            setProperty("gigachat.composedCredentials", credentials);
            load();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public void createDefaults() {
        var props = MainSettingsStore.getDefaltProperties();
        FileSettingsStore.storeProps(props, settingsFile);
    }

    public void setProperty(String key, String value) {
        log.info("Setting properties {} to {}", key, value);
        properties.setProperty(key, value);
        storeProps();
    }

    @Override
    public Properties storeSettings(MainSettings mainSettings) {
        var props = new Properties();
        props.put(MainSettingField.JWT_EXPIRES_AT.getPropertyKey(), String.valueOf(mainSettings.getJwt().getExpiresAt()));
        props.put(MainSettingField.JWT_ACCESS_TOKEN.getPropertyKey(), mainSettings.getJwt().getAccessToken());
        props.put(MainSettingField.COMPOSED_CREDENTIALS.getPropertyKey(), mainSettings.getComposedCredentials());
        props.put(MainSettingField.AUTH_URI.getPropertyKey(), mainSettings.getAuthUri());
        props.put(MainSettingField.TARGET.getPropertyKey(), mainSettings.getTarget());
        props.put(MainSettingField.MODEL.getPropertyKey(), mainSettings.getModel());
        FileSettingsStore.storeProps(props, settingsFile);
        return props;
    }

    @Override
    protected MainSettings initSettings() {
        load();
        var jwt = new MainSettings.Jwt();
        var appSettings = new MainSettings();
        appSettings.setComposedCredentials(getProperty(MainSettingField.COMPOSED_CREDENTIALS.getPropertyKey()));
        appSettings.setAuthUri(getProperty(MainSettingField.AUTH_URI.getPropertyKey()));
        appSettings.setTarget(getProperty(MainSettingField.TARGET.getPropertyKey()));
        appSettings.setModel(getProperty(MainSettingField.MODEL.getPropertyKey()));
        jwt.setAccessToken(getProperty(MainSettingField.JWT_ACCESS_TOKEN.getPropertyKey()));
        jwt.setExpiresAt(Long.parseLong(getProperty(MainSettingField.JWT_EXPIRES_AT.getPropertyKey())));
        appSettings.setJwt(jwt);
        return appSettings;
    }

    @Override
    protected Path initSettingsFile() {
        return Application.SETTINGS_FILE;
    }
}