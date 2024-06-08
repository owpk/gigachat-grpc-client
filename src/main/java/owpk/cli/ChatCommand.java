package owpk.cli;

import io.micronaut.logging.LogLevel;
import io.micronaut.logging.LoggingSystem;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import owpk.role.*;
import owpk.service.ChatHistoryService;
import owpk.service.RetryingChatWrapper;
import owpk.storage.app.RolesStorage;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import static owpk.Application.showApiDocsHelp;

@Slf4j
@CommandLine.Command(versionProvider = VersionProvider.class, name = "gigachat",
        description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ConfigCommand.class, ModelCommand.class, HistoryCommand.class})
public class ChatCommand implements Runnable {
    private final LoggingSystem loggingSystem;
    private final RetryingChatWrapper retryingChatWrapper;
    private final RolesStorage rolesStorage;
    private final ChatHistoryService chatHistoryService;

    private Supplier<RolePrompt> roleSupplier =
            () -> new DefaultChatRolePrompt(collapseInput(), 6);

    @Inject
    public ChatCommand(LoggingSystem loggingSystem,
                       RetryingChatWrapper retryingChatWrapper,
                       RolesStorage rolesStorage, ChatHistoryService chatHistoryService) {
        this.loggingSystem = loggingSystem;
        this.retryingChatWrapper = retryingChatWrapper;
        this.rolesStorage = rolesStorage;
        this.chatHistoryService = chatHistoryService;
    }

    @CommandLine.Option(names = {"-v", "--version"}, versionHelp = true, description = "Print version info.")
    boolean versionRequested;

    @CommandLine.Parameters(description = "User query")
    String[] query;

    @CommandLine.Option(names = {"-c", "--code"}, description = "Set code mode. Return only code snippet.")
    public void setCodeMode(boolean codeMode) {
        if (codeMode) {
            roleSupplier = () -> SystemRolePrompt.create(collapseInput(),
                    rolesStorage.getRole(CodeRolePrompt.NAME));
        }
    }

    @CommandLine.Option(names = {"-s", "--shell"}, description = "Set shell mode. Return only shell command base on your os and shell names.")
    public void setShellMode(boolean shellMode) {
        if (shellMode) {
            roleSupplier = () -> SystemRolePrompt.create(collapseInput(),
                    rolesStorage.getRole(ShellRolePrompt.NAME));
        }
    }

    @CommandLine.Option(names = {"-d", "--describe-shell"}, description = "Set shell mode. Describes shell command.")
    public void setDescribeShellCommand(boolean describeShellCommand) {
        if (describeShellCommand) {
            roleSupplier = () -> SystemRolePrompt.create(collapseInput(),
                    rolesStorage.getRole(DescribeRolePrompt.NAME));
        }
    }

    @CommandLine.Option(names = {"-r", "--custom-role"}, description = "Apply user defined role as system prompt.")
    public void customRole(String roleName) throws IllegalArgumentException {
        if (roleName != null && !roleName.isBlank()) {
            roleSupplier = () -> SystemRolePrompt.create(collapseInput(),
                    rolesStorage.getRole(DescribeRolePrompt.NAME));
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

    @CommandLine.Option(names = {"-n", "--new"}, description = "Create new chat")
    public void createNewChat(boolean createNewChat) {
        if (createNewChat)
            chatHistoryService.createNewChat();
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

        var role = roleSupplier.get();
        var query = role.getUserQuery();

        if (query == null || query.isBlank())
            return;

        var pipedInput = new StringBuilder();
        try (var in = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while (in.ready() && (line = in.readLine()) != null) {
                pipedInput.append(line).append("\n");
            }
        } catch (Exception e) {
            log.info("Error while reading piped input", e);
        }
        
        role.setUserQuery(role.getUserQuery() + 
            (pipedInput.isEmpty() ? "" : " " + pipedInput));

        LoggingUtils.cliCommandLog(this.getClass(), log);
        retryingChatWrapper.chat(role);
    }
}
