package owpk.cli;

import owpk.storage.SettingsStore;
import picocli.CommandLine;

@CommandLine.Command(name = "config", description = "Create or show config.")
public class ConfigCommand implements Runnable {
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
        if (showProperties)
            SettingsStore.INSTANCE.getProperties()
                    .forEach((k, v) -> System.out.println(k + " : " + v));
        else if (createConfig) {
            if (force) {
                SettingsStore.INSTANCE.createDefaults();
            } else {
                var file = SettingsStore.INSTANCE.getSettingsFile();
                if (SettingsStore.INSTANCE.getSettingsFile().exists()) {
                    System.out.println("Settings file exists: " + file.getAbsolutePath());
                    System.out.println("Use -f or --force to force override");
                }
            }
        } else if (credentials != null && !credentials.isBlank()) {
            SettingsStore.INSTANCE.setProperty("gigachat.composedCredentials", credentials);
            System.out.println("Credentials saved successfully!");
        }
    }
}
