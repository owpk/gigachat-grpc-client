package owpk.cli;

import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import owpk.ChatRoles;
import owpk.utils.LoggingUtils;
import owpk.RolePromptAction;
import owpk.service.RetryingChatWrapper;
import picocli.CommandLine;

import java.util.function.Function;

import static owpk.Application.showApiDocsHelp;

@Slf4j
@CommandLine.Command(name = "gigachat", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ConfigCommand.class, ModelCommand.class, HistoryCommand.class})
public class ChatCommand implements Runnable {
    private final LoggingSystem loggingSystem;
    private final RetryingChatWrapper retryingChatWrapper;
    private Function<String, RolePromptAction> chatRoleClosure = ChatRoles.of(ChatRoles.CHAT);
    private int cacheLines = 20;

    @CommandLine.Parameters(defaultValue = "", description = "User query")
    String query;

    // TODO add description
    @CommandLine.Option(names = {"-c", "--code"}, description = "Set code mode. Return only code snippet.")
    public void setCodeMode(boolean codeMode) {
        if (codeMode)
            chatRoleClosure = ChatRoles.of(ChatRoles.CODE);
    }

    // TODO add description
    @CommandLine.Option(names = {"-s", "--shell"}, description = "Set shell mode. Return only shell command base on your os and shell names.")
    public void setShellMode(boolean shellMode) {
        if (shellMode) {
            setNoContext(true);
            chatRoleClosure = ChatRoles.of(ChatRoles.SHELL);
        }
    }

    @CommandLine.Option(names = {"-d", "--describe-shell"}, description = "Set shell mode. Describes shell command.")
    public void setDescribeShellCommand(boolean describeShellCommand) {
        if (describeShellCommand) {
            setNoContext(true);
            chatRoleClosure = ChatRoles.of(ChatRoles.DESCRIBE_SHELL);
        }
    }

    @Inject
    public ChatCommand(LoggingSystem loggingSystem, RetryingChatWrapper retryingChatWrapper) {
        this.loggingSystem = loggingSystem;
        this.retryingChatWrapper = retryingChatWrapper;
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

    @CommandLine.Option(names = {"-u", "--unary"},
            description = "Use unary response type. Default type is stream", defaultValue = "false")
    public void useUnary(boolean useUnary) {
        if (useUnary) retryingChatWrapper.setUnaryMode();
        else retryingChatWrapper.setStreamMode();
    }

    @CommandLine.Option(names = "--log-level", description = "Set log level: ERROR | INFO | DEBUG",
            defaultValue = "ERROR", scope = CommandLine.ScopeType.INHERIT)
    public void setLogLevel(String logLevel) {
        loggingSystem.setLogLevel(Logger.ROOT_LOGGER_NAME, LogLevel.valueOf(logLevel));
    }

    @CommandLine.Option(names = {"--no-context", "-n"}, description = "Disable chat history context")
    public void setNoContext(boolean noCache) {
        if (noCache)
            cacheLines = 0;
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        if (!query.isBlank()) {
            retryingChatWrapper.chat(chatRoleClosure.apply(query), cacheLines);
        } else
            CommandLine.usage(this, System.out);
    }
}
