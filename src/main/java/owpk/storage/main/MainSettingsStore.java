package owpk.storage.main;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import owpk.Application;
import owpk.storage.FileSettingsStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

@Getter
@Slf4j
public class MainSettingsStore implements FileSettingsStore<MainSettings> {

    private static final String NOT_VALID_JWT = "empty_jwt";

    private final Path settingsFile = Application.SETTINGS_FILE;

    private final Properties properties;
    private final MainSettings mainSettings;

    public MainSettingsStore() {
        this.properties = new Properties();
        mainSettings = loadSettings();
    }

    public static Properties getDefaltProperties() {
        log.info("Creating default application store properties...");
        var props = new Properties();
        props.putAll(MainSettingField.propertiesMap);
        return props;
    }

    public void init() {
        log.info("Init application settings store: " + settingsFile);
        load();
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

    public void setDefaults() {
        log.info("Setting default application properties...");
        var jwtProperty = getProperty(MainSettingField.JWT_ACCESS_TOKEN.getPropertyKey());
        if (jwtProperty == null || jwtProperty.trim().isBlank()) {
            setProperty(MainSettingField.JWT_ACCESS_TOKEN.getPropertyKey(), NOT_VALID_JWT);
            log.info("Setting default 'not valid' jwt token because current value is null");
        }
        var expiresProperty = getProperty(MainSettingField.JWT_EXPIRES_AT.getPropertyKey());
        if (expiresProperty == null || expiresProperty.trim().isBlank()) {
            setProperty(MainSettingField.JWT_EXPIRES_AT.getPropertyKey(), "0");
            log.info("Setting default 0 expiration jwt time because current value is null");
        }
    }

    private void load() {
        if (Files.exists(settingsFile)) {
            try (var fis = new FileInputStream(settingsFile.toFile())) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Properties createDefaults() {
        return MainSettingsStore.getDefaltProperties();
    }

    public void setProperty(String key, String value) {
        log.info("Setting properties {} to {}", key, value);
        properties.setProperty(key, value);
        storeProps();
    }

    private void storeProps() {
        log.info("Storing properties...");
        FileSettingsStore.storeProps(properties, settingsFile);
    }

    @Override
    public MainSettings loadSettings() {
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
    public MainSettings getSettings() {
        return mainSettings;
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

}