package owpk.cli;

import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import owpk.LoggingUtils;
import owpk.service.RetryingChatWrapper;
import owpk.storage.SettingsStore;
import picocli.CommandLine;

import java.util.Scanner;

@Slf4j
@CommandLine.Command(name = "gigachat", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ConfigCommand.class, ModelCommand.class, ChatHistoryCommand.class})
public class GigaChatCommand implements Runnable {

    private final LoggingSystem loggingSystem;

    private final RetryingChatWrapper chatService;
    private final SettingsStore settingsStore;

    @Inject
    public GigaChatCommand(LoggingSystem loggingSystem, RetryingChatWrapper chatService,
                           SettingsStore settingsStore) {
        this.loggingSystem = loggingSystem;
        this.chatService = chatService;
        this.settingsStore = settingsStore;
    }

    @CommandLine.Option(names = {"-h", "--help"}, defaultValue = "false", description = "Display help information.")
    public void setShowHelp(boolean showHelp) {
        if (showHelp) {
            showDocsHelp();
            System.out.println("""
                    \tYou need to write your credentials in 'gigachat.composedCredentials' property (or use config -d <credentials>)
                    \tfind how to retrieve credentials: https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    """);
            CommandLine.usage(this, System.out);
        }
    }

    private void showDocsHelp() {
        System.out.println("""
                Please visit
                \thttps://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                \tfor more information
                """);
    }

    @CommandLine.Parameters(defaultValue = "", index = "0", description = "User query")
    String query;

    @CommandLine.Option(names = {"-u", "--unary"},
            description = "Use unary response type. Default type is stream")
    boolean useUnary;

    @CommandLine.Option(names = "--log-level", description = "Set log level: ERROR | INFO | DEBUG",
            defaultValue = "ERROR", scope = CommandLine.ScopeType.INHERIT)
    public void setLogLevel(String logLevel) {
        loggingSystem.setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.valueOf(logLevel));
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (!settingsStore.validate()) {
            System.out.println(CommandLine.Help.Ansi.AUTO.string(
                    "@|bold,fg(yellow) Specify your credentials!|@"));
            showDocsHelp();
            System.out.print("Input 'Basic' auth: ");
            var input = new Scanner(System.in);
            var credentials = input.nextLine();
            if (credentials == null || credentials.isBlank())
                throw new Error("No credentials specified");
            settingsStore.setProperty("gigachat.composedCredentials", credentials);
            settingsStore.getAppSettings().setComposedCredentials(credentials);
        }

        if (!query.isBlank()) {
            if (useUnary)
                chatService.chat(query);
            else
                chatService.chatStream(query);
        }
    }
}
