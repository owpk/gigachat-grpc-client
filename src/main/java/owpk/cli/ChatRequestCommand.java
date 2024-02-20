package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.LoggingUtils;
import owpk.service.ChatService;
import picocli.CommandLine;

@CommandLine.Command(name = "chat", description = "Chat with GigaChat",
        mixinStandardHelpOptions = true)
@Slf4j
public class ChatRequestCommand implements Runnable {

    @CommandLine.Option(names = {"-u", "--unary"},
            description = "Use unary response type. Default type is stream")
    boolean useUnary;
    @CommandLine.Parameters
    String query;
    private final ChatService chatService;

    @Inject
    public ChatRequestCommand(ChatService chatService) {
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
