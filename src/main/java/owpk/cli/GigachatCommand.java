package owpk.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import owpk.LoggingUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "gigachat", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ChatRequestCommand.class, ConfigCommand.class, ModelCommand.class})
@Slf4j
public class GigachatCommand implements Runnable {

    @CommandLine.Option(names = {"-h", "--help"}, description = "Display help information.")
    boolean showHelp;

    // replacing stdout appender to logging output into console
    @CommandLine.Option(names = "--log-level", description = "Set log level: ERROR | INFO | DEBUG",
            defaultValue = "ERROR", scope = CommandLine.ScopeType.INHERIT)
    public void setLogLevel(String logLevel) {
        var ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        var rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("ROOT");
        rootLogger.detachAppender("STDOUT");

        var ple = new PatternLayoutEncoder();
        ple.setPattern(LoggingUtils.LOGGING_PATTERN);
        ple.setContext(ctx);
        ple.start();

        var appender = new ConsoleAppender<ILoggingEvent>();
        appender.setName("STDOUT_PROPS");
        appender.setEncoder(ple);
        appender.setContext(ctx);
        appender.start();

        rootLogger.addAppender(appender);
        rootLogger.setLevel(Level.valueOf(logLevel));
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
