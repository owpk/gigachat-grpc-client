package owpk.cli;

import lombok.extern.slf4j.Slf4j;
import owpk.properties.BootstrapPropertiesFactory;
import owpk.properties.concrete.CredentialProps;
import owpk.properties.concrete.MainProps;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "config", aliases = {"cfg", "conf"}, description = "Create or show config.")
@Slf4j
public class ConfigCommand implements Runnable {
    @CommandLine.Option(names = {"-s", "--show"}, description = "Show this help message and exit.")
    boolean showProperties;
    @CommandLine.Option(names = {"-c", "--create"}, description = "Rewrite settings file with default config.")
    boolean createConfig;
    @CommandLine.Option(names = {"-d", "--credentials"}, description = "Set credentials property.")
    String credentials;

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (showProperties) {
            var mainProps = BootstrapPropertiesFactory.getInstance().getProvider(MainProps.class);
            mainProps.getProperties().entrySet().stream()
                    .forEach((e) -> System.out.println(e.getKey() + " : " + e.getValue()));
        }
        else if (createConfig) {
            var mainProps = BootstrapPropertiesFactory.getInstance().getProvider(MainProps.class);
            mainProps.createDefaults();
            System.out.println("Settings file rewritten successfully!");
        } else if (credentials != null && !credentials.isBlank()) {
            var creds = BootstrapPropertiesFactory.getInstance().getProvider(CredentialProps.class);
            creds.setProperty(CredentialProps.DEF_COMPOSED_CREDENTIALS, credentials);
            System.out.println("Credentials saved successfully!");
        }
    }
}