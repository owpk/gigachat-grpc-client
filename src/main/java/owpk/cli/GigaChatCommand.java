package owpk.cli;

import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import owpk.LoggingUtils;
import owpk.UserRoles;
import owpk.service.RetryingChatWrapper;
import picocli.CommandLine;

import static owpk.Application.showApiDocsHelp;

@Slf4j
@CommandLine.Command(name = "gigachat", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ConfigCommand.class, ModelCommand.class, ChatHistoryCommand.class})
public class GigaChatCommand implements Runnable {

    private final LoggingSystem loggingSystem;

    private final RetryingChatWrapper chatService;

    @Inject
    public GigaChatCommand(LoggingSystem loggingSystem, RetryingChatWrapper chatService) {
        this.loggingSystem = loggingSystem;
        this.chatService = chatService;
    }

    @CommandLine.Option(names = {"-h", "--help"}, defaultValue = "false", description = "Display help information.")
    public void setShowHelp(boolean showHelp) {
        if (showHelp) {
            showApiDocsHelp();
            System.out.println("""
                    \tYou need to write your credentials in 'gigachat.composedCredentials' property (or use config -d <credentials>)
                    \tfind how to retrieve credentials: https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    """);
            CommandLine.usage(this, System.out);
        }
    }

    @CommandLine.Parameters(defaultValue = "", description = "User query")
    String query;

    @CommandLine.Option(names = {"-u", "--unary"},
            description = "Use unary response type. Default type is stream", defaultValue = "false")
    public void useUnary(boolean useUnary) {
        if (useUnary) chatService.setUnaryMode();
        else chatService.setStreamMode();
    }

    @CommandLine.Option(names = "--log-level", description = "Set log level: ERROR | INFO | DEBUG",
            defaultValue = "ERROR", scope = CommandLine.ScopeType.INHERIT)
    public void setLogLevel(String logLevel) {
        loggingSystem.setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.valueOf(logLevel));
    }

    // TODO add description
    @CommandLine.Option(names = {"-c", "--code"}, description = "Set code mode. Return only code snippet.")
    boolean codeMode;

    // TODO add description
    @CommandLine.Option(names = {"-s", "--shell"}, description = "Set shell mode. Return only shell command base on your os and shell names.")
    boolean shellMode;

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (!query.isBlank()) {
            if (codeMode) {
                log.info("Running in code mode");
                chatService.chat(UserRoles.of(UserRoles.CODE).apply(query));
            } else if (shellMode) {
                log.info("Running in shell mode");
                chatService.chat(UserRoles.of(UserRoles.SHELL).apply(query));
            } else {
                log.info("Running in chat mode");
                chatService.chat(UserRoles.of(UserRoles.CHAT).apply(query), 4);
            }
        }
    }
}
