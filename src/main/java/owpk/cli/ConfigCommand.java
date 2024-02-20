package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.LoggingUtils;
import owpk.storage.SettingsStore;
import picocli.CommandLine;

@CommandLine.Command(name = "config", aliases = {"cfg", "conf"}, description = "Create or show config.")
@Slf4j
public class ConfigCommand implements Runnable {
    private final SettingsStore settingsStore;

    @Inject
    public ConfigCommand(SettingsStore settingsStore) {
        this.settingsStore = settingsStore;
    }

    @CommandLine.Option(names = {"-s", "--show"}, description = "Show this help message and exit.")
    boolean showProperties;

    @CommandLine.Option(names = {"-c", "--create"}, description = "Create default config.")
    boolean createConfig;

    @CommandLine.Option(names = {"-f", "--force"}, description = "Force override existing config or create new if not exists.")
    boolean force;

    @CommandLine.Option(names = {"-d", "--credentials"}, description = "Set credentials property.")
    String credentials;

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (showProperties)
            settingsStore.getProperties()
                    .forEach((k, v) -> System.out.println(k + " : " + v));
        else if (createConfig) {
            if (force) {
                settingsStore.writeDefaultProperties();
                System.out.println("Settings file rewritten successfully!");
            } else {
                var file = settingsStore.getSettingsFile();
                if (settingsStore.getSettingsFile().exists()) {
                    System.out.println("Settings file exists: " + file.getAbsolutePath());
                    System.out.println("Use -f or --force to force override");
                }
            }
        } else if (credentials != null && !credentials.isBlank()) {
            settingsStore.setProperty("gigachat.composedCredentials", credentials);
            System.out.println("Credentials saved successfully!");
        }
    }
}
