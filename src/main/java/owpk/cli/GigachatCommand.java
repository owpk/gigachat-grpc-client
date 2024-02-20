package owpk.cli;

import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import owpk.LoggingUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "gigachat", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ChatRequestCommand.class, ConfigCommand.class, ModelCommand.class})
@Slf4j
public class GigachatCommand implements Runnable {

    private final LoggingSystem loggingSystem;

    @Inject
    public GigachatCommand(LoggingSystem loggingSystem) {
        this.loggingSystem = loggingSystem;
    }

    @CommandLine.Option(names = {"-h", "--help"}, description = "Display help information.")
    boolean showHelp;

    // replacing stdout appender to logging output into console
    @CommandLine.Option(names = "--log-level", description = "Set log level: ERROR | INFO | DEBUG",
            defaultValue = "INFO", scope = CommandLine.ScopeType.INHERIT)
    public void setLogLevel(String logLevel) {
        loggingSystem.setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.valueOf(logLevel));
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (showHelp) {
            System.out.println("""
                    Please visit
                    \thttps://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    \tfor more information
                    """);
            System.out.println("""
                    Use config -s to show config file properties
                    \tYou need to write your credentials in 'gigachat.composedCredentials' property
                    \tfind how to retrieve credentials: https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    """);
            CommandLine.usage(this, System.out);
        }
    }
}
