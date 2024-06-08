package owpk.service;

import gigachat.v1.Gigachatv1;
import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.grpc.GigaChatGRpcClient;
import owpk.role.RolePrompt;
import owpk.settings.main.MainSettings;
import owpk.storage.LocalStorage;
import owpk.storage.Storage;
import owpk.storage.app.MainSettingsStore;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ChatServiceImpl implements ChatService {
    protected final GigaChatGRpcClient gigaChatGRpcClient;
    protected final MainSettings mainSettings;
    protected final ChatHistoryService chatHistoryService;
    private ChatRequestHandler chatRequestHandler;

    public ChatServiceImpl(GigaChatGRpcClient gigaChatGRpcClient,
                           ChatHistoryService chatHistoryService,
                           MainSettingsStore mainSettingsStore) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        this.chatHistoryService = chatHistoryService;
        this.mainSettings = mainSettingsStore.getSettings();
        this.chatRequestHandler = streamMode();
    }

    @Override
    public void setStreamMode() {
        chatRequestHandler = streamMode();
    }

    @Override
    public void setUnaryMode() {
        chatRequestHandler = unaryMode();
    }

    // default mode is stream
    protected ChatRequestHandler streamMode() {
        return request -> {
            var sw = new StringWriter();
            var iterator = gigaChatGRpcClient.chatStream(request);
            while (iterator.hasNext()) {
                var msg = iterator.next();
                var content = defaultHandleResponse(msg);
                sw.append(content);
                System.out.print(content);
            }
            System.out.println();
            return sw.toString();
        };
    }

    protected ChatRequestHandler unaryMode() {
        return request -> {
            var response = gigaChatGRpcClient.chat(request);
            var content = defaultHandleResponse(response);
            System.out.println(content);
            return content;
        };
    }

    @Override
    public void chat(RolePrompt rolePrompt) {
        baseChatRequest(rolePrompt);
    }

    protected Gigachatv1.ChatRequest createdRequest(Gigachatv1.Message userRequestMsg,
                                                    List<Gigachatv1.Message> additionalMessages) {
        return Gigachatv1.ChatRequest.newBuilder()
                .setModel(mainSettings.getModel())
                .addAllMessages(additionalMessages)
                .addMessages(userRequestMsg)
                .build();
    }

    protected Gigachatv1.Message buildMessage(String content, String role) {
        return Gigachatv1.Message.newBuilder()
                .setRole(role)
                .setContent(content)
                .build();
    }

    protected void persistResponse(String content) {
        chatHistoryService.persistContentToHistory(content,
                GigaChatConstants.MessageRole.ASSISTANT.getValue());
    }

    protected void persistRequest(String content) {
        chatHistoryService.persistContentToHistory(content,
                GigaChatConstants.MessageRole.USER.getValue());
    }

    protected List<Gigachatv1.Message> readLastMessages(int messageCount) {
        if (messageCount == 0)
            return new ArrayList<>();
        return chatHistoryService.readLastMessages(messageCount, true, false)
                .stream().map(it -> buildMessage(it.content(), it.role()))
                .collect(Collectors.toList());
    }

    protected void baseChatRequest(RolePrompt rolePrompt) {
        log.info("Chat request: " + rolePrompt);

        var lastMessages = readLastMessages(rolePrompt.getChatHistoryContextSize());
        persistRequest(rolePrompt.getUserQuery());

        var userRequestMsg = buildMessage(rolePrompt.getRolePrompt(),
                GigaChatConstants.MessageRole.USER.getValue());
        var request = createdRequest(userRequestMsg, lastMessages);

        log.info("""
                Building grpc request:
                    Messages count: {}
                    Messages roles: {}
                """, request.getMessagesCount(), request.getMessagesList().stream()
                .map(it -> it.getRole() + " : " + it.getContent().substring(0, it.getContent().length() -
                        (it.getContent().length() / 2)) + "...\n")
                .collect(Collectors.joining(" ")));

        var result = chatRequestHandler.handleChatRequest(request);
        persistResponse(result);
    }

    // TODO handle response alternatives (what is it at all???)
    protected String defaultHandleResponse(Gigachatv1.ChatResponse chatResponse) {
        log.info("Alternatives: {}", chatResponse.getAlternativesCount());
        return chatResponse.getAlternativesList().stream().map(a -> a.getMessage().getContent())
                .collect(Collectors.joining(" "));
    }

    @FunctionalInterface
    public interface ChatRequestHandler {
        String handleChatRequest(Gigachatv1.ChatRequest request);
    }
}
