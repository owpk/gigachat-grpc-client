package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.LoggingUtils;
import owpk.storage.main.MainSettingField;
import owpk.storage.main.MainSettingsStore;
import picocli.CommandLine;

@CommandLine.Command(name = "config", aliases = {"cfg", "conf"}, description = "Create or show config.")
@Slf4j
public class ConfigCommand implements Runnable {
    private final MainSettingsStore mainSettingsStore;

    @Inject
    public ConfigCommand(MainSettingsStore mainSettingsStore) {
        this.mainSettingsStore = mainSettingsStore;
    }

    @CommandLine.Option(names = {"-s", "--show"}, description = "Show this help message and exit.")
    boolean showProperties;

    @CommandLine.Option(names = {"-c", "--create"}, description = "Rewrite settings file with default config.")
    boolean createConfig;

    @CommandLine.Option(names = {"-d", "--credentials"}, description = "Set credentials property.")
    String credentials;

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (showProperties)
            mainSettingsStore.getProperties()
                    .forEach((k, v) -> System.out.println(k + " : " + v));
        else if (createConfig) {
            mainSettingsStore.createDefaults();
            System.out.println("Settings file rewritten successfully!");
        } else if (credentials != null && !credentials.isBlank()) {
            mainSettingsStore.setProperty(MainSettingField.COMPOSED_CREDENTIALS.name(), credentials);
            System.out.println("Credentials saved successfully!");
        }
    }
}