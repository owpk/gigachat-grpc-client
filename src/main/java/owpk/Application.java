package owpk;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import owpk.cli.GigaChatCommand;
import owpk.storage.SettingsStore;

import java.io.File;
import java.io.IOException;

@Slf4j
public class Application {

    public static final String userHome = System.getProperty("user.home");
    public static final String appHome = userHome + File.separator + ".gigachat-cli";
    public static final String settingsHome = appHome + File.separator + "gigachat.properties";
    public static File homeDir = new File(appHome);
    public static File settingsFile;


    private static void init() throws IOException {
        initConfigHome();
        System.setProperty("micronaut.config.files", settingsHome);
    }

    private static void initConfigHome() throws IOException {
        settingsFile = new File(settingsHome);
        var parent = settingsFile.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new IllegalStateException("Couldn't create dir: " + parent);

        if (!settingsFile.exists()) {
            if (settingsFile.createNewFile()) {
                System.out.println("Creating new settings file: " + settingsFile.getAbsolutePath());
                var defaults = SettingsStore.getDefaltProperties();
                SettingsStore.storeProps(defaults, settingsFile);
            } else {
                throw new IllegalStateException("Cannot create settings file");
            }
        }
    }


    public static void main(String[] args) throws IOException {
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