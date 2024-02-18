package owpk;

import io.micronaut.configuration.picocli.PicocliRunner;
import owpk.cli.GigachatCommand;
import owpk.storage.SettingsStore;

// TODO chat history
public class Application {

    public static void main(String[] args) {
        try {
            SettingsStore.init();
            SettingsStore.setDefaults();
        } catch (Exception e) {
            System.out.println("Error: " + e.getLocalizedMessage());
            return;
        }
        PicocliRunner.run(GigachatCommand.class, args);
    }
}