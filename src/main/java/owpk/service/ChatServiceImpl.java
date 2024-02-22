package owpk.service;

import gigachat.v1.Gigachatv1;
import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.RolePromptAction;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ChatServiceImpl implements ChatService {
    protected final GigaChatGRpcClient gigaChatGRpcClient;
    protected final SettingsStore settingsStore;
    protected final ChatHistoryService chatHistoryService;
    private ChatRequestHandler chatRequestHandler;

    @FunctionalInterface
    public interface ChatRequestHandler {
        String handleChatRequest(Gigachatv1.ChatRequest request);
    }

    public ChatServiceImpl(GigaChatGRpcClient gigaChatGRpcClient,
                           ChatHistoryService chatHistoryService,
                           SettingsStore settingsStore) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        this.chatHistoryService = chatHistoryService;
        this.settingsStore = settingsStore;
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
    public void chat(RolePromptAction promptRole) {
        baseChatRequest(promptRole, Collections.emptyList());
    }

    @Override
    public void chat(RolePromptAction promptRole, int lastMessageCount) {
        baseChatRequest(promptRole, readLastMessages(lastMessageCount));
    }

    protected Gigachatv1.ChatRequest.Builder createdRequestBuilder(List<Gigachatv1.Message> additionalMessages) {
        return Gigachatv1.ChatRequest.newBuilder()
                .setModel(settingsStore.getAppSettings().getModel())
                .addAllMessages(additionalMessages);
    }

    protected Gigachatv1.Message buildMessage(String content, String role) {
        return Gigachatv1.Message.newBuilder()
                .setRole(role)
                .setContent(content)
                .build();
    }

    protected void persistResponse(String content) {
        chatHistoryService.persistContentToHistory(content,
                GigaChatConstants.MessageRole.ASSISTANT);
    }

    protected void persistRequest(String content, String roleName) {
        chatHistoryService.persistContentToHistory(content, roleName);
    }

    protected List<Gigachatv1.Message> readLastMessages(int messageCount) {
        return chatHistoryService.readLastMessages(messageCount, true, false)
                .stream().map(it -> buildMessage(it.content(), it.role()))
                .collect(Collectors.toList());
    }

    protected void baseChatRequest(RolePromptAction rolePromptAction,
                                   List<Gigachatv1.Message> messages) {
        persistRequest(rolePromptAction.getRolePrompt(),
                rolePromptAction.getMessageRole());

        var request = createdRequestBuilder(messages);
        request.addMessages(buildMessage(rolePromptAction.getRolePrompt(),
                rolePromptAction.getMessageRole()));

        log.info("""
                Building grpc request:
                    Messages count: {}
                    Messages: {}
                """, request.getMessagesCount(), request.getMessagesList());

        var result = chatRequestHandler.handleChatRequest(request.build());
        persistResponse(result);
    }

    // TODO handle response alternatives (what is it at all???)
    protected String defaultHandleResponse(Gigachatv1.ChatResponse chatResponse) {
        return chatResponse.getAlternativesList().stream().map(a -> a.getMessage().getContent())
                .collect(Collectors.joining(" "));
    }
}
