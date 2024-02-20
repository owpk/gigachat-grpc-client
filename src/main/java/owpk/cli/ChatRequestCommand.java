package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.LoggingUtils;
import owpk.service.ChatService;
import owpk.service.RetryingChatWrapper;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "chat", aliases = {"ch"}, description = "Chat with GigaChat.",
        mixinStandardHelpOptions = true)
public class ChatRequestCommand implements Runnable {

    @CommandLine.Option(names = {"-u", "--unary"},
            description = "Use unary response type. Default type is stream")
    boolean useUnary;
    @CommandLine.Parameters
    String query;
    private final RetryingChatWrapper chatService;

    @Inject
    public ChatRequestCommand(RetryingChatWrapper chatService) {
        this.chatService = chatService;
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);

        if (useUnary)
            chatService.chat(query);
        else
            chatService.chatStream(query);
    }
}
