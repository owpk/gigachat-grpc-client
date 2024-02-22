package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.LoggingUtils;
import owpk.service.ChatHistoryService;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@CommandLine.Command(name = "history", aliases = {"hs", "h"}, description = "Show chat history.",
        mixinStandardHelpOptions = true)
public class ChatHistoryCommand implements Runnable {

    private final ChatHistoryService chatHistoryService;

    @Inject
    public ChatHistoryCommand(ChatHistoryService chatHistoryService) {
        this.chatHistoryService = chatHistoryService;
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        var messages = chatHistoryService.readLastMessages(true, true);
        log.info("Chat history: " + messages.size());

        messages.stream().map(it -> CommandLine.Help.Ansi.AUTO.string(String.format(
                        "@|bold,fg(green) %s:|@%n %s%n", it.role(), it.content()))
        ).forEach(System.out::println);
    }
}
