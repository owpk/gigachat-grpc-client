package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.model.ChatMessage;
import owpk.service.ChatHistoryService;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

import java.util.List;

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
    void clearHistory(boolean clean) {
        if(clean)
            chatHistoryService.clearChatHistory();
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        List<ChatMessage> messages = chatHistoryService.readLastMessages(true, true);
        log.info("Chat history: {}", messages.size());
        messages.stream().map(it -> CommandLine.Help.Ansi.AUTO.string(String.format(
                "@|bold,fg(green) %s:|@%n %s%n", it.role(), it.content()))
        ).forEach(System.out::println);
    }
}
