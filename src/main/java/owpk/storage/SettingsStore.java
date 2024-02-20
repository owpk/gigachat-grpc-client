package owpk.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

@Slf4j
public class SettingsStore {
    public static final SettingsStore INSTANCE = new SettingsStore();
    private static final String NOT_VALID_JWT = "empty_jwt";
    private final String settingsHome = System.getProperty("user.home") +
            "/.gigachat-cli/gigachat.properties";

    @Getter
    private File settingsFile;

    @Getter
    private Properties properties;


    public static void init() {
        log.info("Init application settings store: " + INSTANCE.settingsHome);
        INSTANCE.properties = new Properties();
        try {
            INSTANCE.settingsFile = new File(INSTANCE.settingsHome);
            var file = INSTANCE.settingsFile;
            var parent = file.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs())
                throw new IllegalStateException("Couldn't create dir: " + parent);

            if (file.exists()) {
                try (var fis = new FileInputStream(file)) {
                    INSTANCE.properties.load(fis);
                }
            } else {
                file.createNewFile();
                INSTANCE.createDefaults();
            }
            System.setProperty("micronaut.config.files", INSTANCE.settingsHome);
        } catch (IOException e) {
            log.error("Error while init properties.", e);
        }
    }

    public static void validate() {
        log.info("Validating application settings store properties...");
        INSTANCE.load();
        if (INSTANCE.getProperty("gigachat.composedCredentials").isEmpty()) {
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

    public static void setDefaults() {
        log.info("Setting default application properties...");
        var jwtProperty = INSTANCE.getProperty("gigachat.jwt.accessToken");
        if (jwtProperty == null || jwtProperty.trim().isBlank()) {
            INSTANCE.setProperty("gigachat.jwt.accessToken", NOT_VALID_JWT);
            log.info("Setting default 'not valid' jwt token because current value is null");
        }
        var expiresProperty = INSTANCE.getProperty("gigachat.jwt.expiresAt");
        if (expiresProperty == null || expiresProperty.trim().isBlank()) {
            INSTANCE.setProperty("gigachat.jwt.expiresAt", "0");
            log.info("Setting default 0 expiration jwt time because current value is null");
        }
    }

    private void load() {
        if (settingsFile.exists()) {
            try (var fis = new FileInputStream(settingsHome)) {
                INSTANCE.properties.load(fis);
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

    public void createDefaults() {
        log.info("Creating default application store properties...");
        properties.put("gigachat.composedCredentials", "");
        properties.put("gigachat.model", "GigaChat:latest");
        properties.put("gigachat.target", "gigachat.devices.sberbank.ru");
        properties.put("gigachat.jwt.accessToken", NOT_VALID_JWT);
        properties.put("gigachat.jwt.expiresAt", "0");
        properties.put("gigachat.authUri", "https://ngw.devices.sberbank.ru:9443/api/v2/oauth");
        properties.put("micronaut.application.name", "gigachat-cli");
        storeProps();
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        storeProps();
    }

    private void storeProps() {
        log.info("Storing properties...");
        try (var fos = new FileOutputStream(settingsHome)) {
            properties.store(fos, null);
        } catch (IOException e) {
            log.error("Error while storing properties.", e);
        }
    }
}