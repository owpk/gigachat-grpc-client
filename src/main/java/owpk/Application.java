package owpk;

import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import owpk.cli.ChatCommand;
import owpk.cli.Color;
import owpk.cli.ConsoleWriter;
import owpk.properties.BootstrapPropertiesFactory;
import owpk.properties.PropertyValidationException;

@Slf4j
public class Application {
    public static void main(String[] args) {

        try {
            BootstrapPropertiesFactory.validateProviders();
        } catch (PropertyValidationException e) {
            ConsoleWriter.writeLn(e.getMessage(), Color.RED);
        }

        try {
            PicocliRunner.run(ChatCommand.class, args);
        } catch (Throwable e) {
            log.info("Error while running command.", e);
            System.out.println("Error while running command: " + e.getLocalizedMessage());
        }
    }
}