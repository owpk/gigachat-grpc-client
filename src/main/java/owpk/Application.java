package owpk;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import io.micronaut.configuration.picocli.PicocliRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import owpk.cli.GigachatCommand;
import owpk.storage.SettingsStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

// TODO chat history
public class Application {

    private static final String userHome = System.getProperty("user.home");
    private static final String appHome = userHome + File.separator + ".gigachat-cli";
    private static final String settingsHome = appHome + File.separator + "gigachat.properties";

    private static File homeDir;
    public static File settingsFile;


    private static void init() throws IOException {
        initConfigHome();
        initFileLogger();
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

    private static void initFileLogger() {
        var ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
        var rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        var ple = new PatternLayoutEncoder();
        ple.setPattern(LoggingUtils.LOGGING_PATTERN);
        ple.setContext(ctx);
        ple.start();

        var appender = new FileAppender<ILoggingEvent>();
        appender.setName("FILE");
        appender.setFile(Paths.get(appHome,  "gigachat.log").toString());
        appender.setEncoder(ple);
        appender.setContext(ctx);
        appender.start();

        rootLogger.addAppender(appender);
    }

    public static void main(String[] args) throws IOException {
        init();
        PicocliRunner.run(GigachatCommand.class, args);
    }
}