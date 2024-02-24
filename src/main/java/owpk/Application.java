package owpk;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import owpk.cli.GigaChatCommand;
import owpk.storage.FileSettingsStore;
import owpk.storage.main.MainSettingsStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class Application {
    public static final String USER_HOME = System.getProperty("user.home");
    private static final String APP_HOME_NAME = ".gigachat-cli";
    public static final Path APP_HOME_DIR = Paths.get(USER_HOME, APP_HOME_NAME);
    private static final String APP_CONFIG_NAME = "gigachat.properties";
    public static final Path SETTINGS_FILE = Paths.get(APP_HOME_DIR.toString(), APP_CONFIG_NAME);
    public static String osName;

    private static void init() throws IOException {
        try {
            var rawOsName = Files.readString(Paths.get("/etc/issue"));
            osName = rawOsName.substring(0, rawOsName.indexOf("\\")).trim();
        } catch (IOException e) {
            osName = System.getProperty("os.name");
        }
        initConfigHome();
    }

    private static void initConfigHome() throws IOException {
        if (!Files.exists(APP_HOME_DIR) && !Files.exists(Files.createDirectories(APP_HOME_DIR)))
            throw new IllegalStateException("Couldn't create dir: " + APP_HOME_DIR);
        if (!Files.exists(SETTINGS_FILE)) {
            if (!Files.exists(Files.createFile(SETTINGS_FILE)))
                throw new IllegalStateException("Couldn't create dir: " + APP_HOME_DIR);
            else {
                System.out.println("Creating new settings file: " + SETTINGS_FILE);
                var defaults = MainSettingsStore.getDefaltProperties();
                FileSettingsStore.storeProps(defaults, SETTINGS_FILE);
            }
        }
    }

    public static void main(String[] args) {
        try {
            init();
            PicocliRunner.run(GigaChatCommand.class, args);
        } catch (Throwable e) {
            log.info("Error while running command.", e);
            System.out.println("Error while running command: " + e.getLocalizedMessage());
        }
    }

    public static void showApiDocsHelp() {
        System.out.println("""
                Please visit
                \thttps://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                \tfor more information
                """);
    }

}