package owpk.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import owpk.Application;
import owpk.config.AppSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Getter
@Slf4j
public class SettingsStore {
    private static final String NOT_VALID_JWT = "empty_jwt";

    private final File settingsFile = Application.settingsFile;

    private Properties properties;

    private final AppSettings appSettings;

    public SettingsStore(AppSettings appSettings) {
        this.appSettings = appSettings;
    }

    public void init() {
        log.info("Init application settings store: " + settingsFile.getAbsolutePath());
        properties = new Properties();
        try {
            try (var fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            }
        } catch (IOException e) {
            log.error("Error while init properties.", e);
            throw new RuntimeException(e);
        }
    }

    public void validate() {
        log.info("Validating application settings store properties...");
        load();
        if (getProperty("gigachat.composedCredentials").isEmpty()) {
            System.out.println("""
                    Error:
                        Missing 'gigachat.composedCredentials'.
                        Use gigachat-cli config -c 'YOUR_CREDENTIALS'
                        OR
                        Set it manually in ~/.gigachat-cli/gigachat.properties.
                        For more info visit:
                        https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    """);
            System.exit(0);
        }
    }

    public void setDefaults() {
        log.info("Setting default application properties...");
        var jwtProperty = getProperty("gigachat.jwt.accessToken");
        if (jwtProperty == null || jwtProperty.trim().isBlank()) {
            setProperty("gigachat.jwt.accessToken", NOT_VALID_JWT);
            log.info("Setting default 'not valid' jwt token because current value is null");
        }
        var expiresProperty = getProperty("gigachat.jwt.expiresAt");
        if (expiresProperty == null || expiresProperty.trim().isBlank()) {
            setProperty("gigachat.jwt.expiresAt", "0");
            log.info("Setting default 0 expiration jwt time because current value is null");
        }
    }

    private void load() {
        if (settingsFile.exists()) {
            try (var fis = new FileInputStream(settingsFile)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getUserHome() {
        return System.getProperty("user.home");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Properties getDefaltProperties() {
        log.info("Creating default application store properties...");
        var props = new Properties();
        props.put("gigachat.composedCredentials", "");
        props.put("gigachat.model", "GigaChat:latest");
        props.put("gigachat.target", "gigachat.devices.sberbank.ru");
        props.put("gigachat.jwt.accessToken", NOT_VALID_JWT);
        props.put("gigachat.jwt.expiresAt", "0");
        props.put("gigachat.authUri", "https://ngw.devices.sberbank.ru:9443/api/v2/oauth");
        props.put("micronaut.application.name", "gigachat-cli");
        return props;
    }

    public void writeDefaultProperties() {
        properties = getDefaltProperties();
        storeProps();
    }

    public void setProperty(String key, String value) {
        log.info("Setting properties {} to {}", key, value);
        properties.setProperty(key, value);
        storeProps();
    }

    public static void storeProps(Properties properties, File settingsFile) {
        try (var fos = new FileOutputStream(settingsFile)) {
            properties.store(fos, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeProps() {
        log.info("Storing properties...");
        storeProps(properties, settingsFile);
    }
}