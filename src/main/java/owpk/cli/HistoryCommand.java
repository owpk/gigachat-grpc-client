package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.service.ChatHistoryService;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "history", aliases = {"hs", "h"}, description = "Show chat history.",
        mixinStandardHelpOptions = true)
public class HistoryCommand implements Runnable {

    private final ChatHistoryService chatHistoryService;

    @Inject
    public HistoryCommand(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @CommandLine.Option(names = {"-c", "--clear"}, description = "Clear history.")
    boolean clearHistory;

    @Override
    public void run() {
        if (!clearHistory) {
            LoggingUtils.cliCommandLog(this.getClass(), log);
            var messages = chatHistoryService.readLastMessages(true, true);
            log.info("Chat history: " + messages.size());

            messages.stream().map(it -> CommandLine.Help.Ansi.AUTO.string(String.format(
                    "@|bold,fg(green) %s:|@%n %s%n", it.role(), it.content()))
            ).forEach(System.out::println);
        } else chatHistoryService.clearChatHistory();
    }
}
