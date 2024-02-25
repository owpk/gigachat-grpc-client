package owpk.cli;

import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import owpk.role.*;
import owpk.service.RetryingChatWrapper;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import static owpk.Application.showApiDocsHelp;

@Slf4j
@CommandLine.Command(name = "gigachat", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ConfigCommand.class, ModelCommand.class, HistoryCommand.class})
public class ChatCommand implements Runnable {
    private final LoggingSystem loggingSystem;
    private final RetryingChatWrapper retryingChatWrapper;
    private Supplier<RolePrompt> roleSupplier =
            () -> new DefaultChatRolePrompt(collapseInput(), 6);

    @Inject
    public ChatCommand(LoggingSystem loggingSystem, RetryingChatWrapper retryingChatWrapper) {
        this.loggingSystem = loggingSystem;
        this.retryingChatWrapper = retryingChatWrapper;
    }

    @CommandLine.Parameters(description = "User query")
    String[] query;

    @CommandLine.Option(names = {"-c", "--code"}, description = "Set code mode. Return only code snippet.")
    public void setCodeMode(boolean codeMode) {
        if (codeMode) {
            roleSupplier = () -> new CodeRolePrompt(collapseInput());
        }
    }

    @CommandLine.Option(names = {"-s", "--shell"}, description = "Set shell mode. Return only shell command base on your os and shell names.")
    public void setShellMode(boolean shellMode) {
        if (shellMode) {
            roleSupplier = () -> new ShellRolePrompt(collapseInput());
        }
    }

    @CommandLine.Option(names = {"-d", "--describe-shell"}, description = "Set shell mode. Describes shell command.")
    public void setDescribeShellCommand(boolean describeShellCommand) {
        if (describeShellCommand) {
            roleSupplier = () -> new DescribeRolePrompt(collapseInput());
        }
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

    private String collapseInput() {
        if (query != null && query.length > 0)
            return String.join(" ", query);
        return "";
    }

    @Override
    public void run() {
        var pipedInput = new StringBuilder();
        try (var in = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while (in.ready() && (line = in.readLine()) != null) {
                pipedInput.append(line).append("\n");
            }
        } catch (Exception e) {
            log.info("Error while reading piped input: " + e);
        }

        var role = roleSupplier.get();
        role.setUserQuery(role.getUserQuery() + " " + pipedInput);
        LoggingUtils.cliCommandLog(this.getClass(), log);
        retryingChatWrapper.chat(role);
    }
}
