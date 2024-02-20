package owpk;

import io.micronaut.configuration.picocli.PicocliRunner;
import owpk.cli.GigachatCommand;
import owpk.storage.SettingsStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// TODO chat history
public class Application {

    private static final String userHome = System.getProperty("user.home");
    private static final String appHome = userHome + File.separator + ".gigachat-cli";
    private static final String settingsHome = appHome + File.separator + "gigachat.properties";

    private static File homeDir;
    public static File settingsFile;


    private static void init() {
        settingsFile = new File(settingsHome);
        var file = settingsFile;
        var parent = file.getParentFile();

        if (parent != null && !parent.exists() && !parent.mkdirs())
            throw new IllegalStateException("Couldn't create dir: " + parent);

        System.setProperty("micronaut.config.files", settingsHome);
    }

    public static void main(String[] args) {
        init();
        PicocliRunner.run(GigachatCommand.class, args);
    }
}