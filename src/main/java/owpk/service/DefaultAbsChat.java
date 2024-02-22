package owpk.service;

import gigachat.v1.Gigachatv1;
import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class DefaultAbsChat implements ChatService {
    protected final GigaChatGRpcClient gigaChatGRpcClient;
    protected final SettingsStore settingsStore;
    protected final ChatHistoryService chatHistoryService;

    public DefaultAbsChat(GigaChatGRpcClient gigaChatGRpcClient,
                                ChatHistoryService chatHistoryService,
                                SettingsStore settingsStore) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        this.chatHistoryService = chatHistoryService;
        this.settingsStore = settingsStore;
    }

    protected Gigachatv1.ChatRequest buildRequest(String query, int lastMessageCount) {
        var lastMessages = chatHistoryService.readLastMessages(lastMessageCount, false)
                .stream().map(it -> buildMessage(it.content(), it.role()))
                .collect(Collectors.toList());
        return buildRequest(query, lastMessages);
    }

    protected Gigachatv1.ChatRequest buildRequest(String query, List<Gigachatv1.Message> additionalMessages) {
        log.info("""
                Building grpc request:
                    Query: {}
                    Last messages count: {}
                    Last messages roles: {}
                """, query, additionalMessages.size(), additionalMessages.stream()
                .map(Gigachatv1.Message::getRole)
                .collect(Collectors.joining(" ")));

        return Gigachatv1.ChatRequest.newBuilder()
                .setModel(settingsStore.getAppSettings().getModel())
                .addAllMessages(additionalMessages)
                .addMessages(Gigachatv1.Message.newBuilder()
                        .setRole(GigaChatConstants.Role.USER)
                        .setContent(query)
                        .build())
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
                GigaChatConstants.Role.ASSISTANT);
    }

    protected void persistRequest(String content) {
        chatHistoryService.persistContentToHistory(content,
                GigaChatConstants.Role.USER);
    }

    // TODO handle response alternatives (what is it at all???)
    protected String handleResponse(Gigachatv1.ChatResponse chatResponse) {
        return chatResponse.getAlternativesList().stream().map(a -> a.getMessage().getContent())
                .collect(Collectors.joining(" "));
    }

    public void chat(String query, int lastMessageCount) {
        persistRequest(query);
        var result = chatBuilder(buildRequest(query, lastMessageCount));
        persistResponse(result);
    }

    public void shell(String query) {
        persistRequest(query);
        var roleRequest = UserRoles.shellPrompt("zsh", "Linux", query);
        var systemMsg = buildMessage(roleRequest, query);
        chatRoleBuilder(systemMsg, query);
        persistResponse("");
    }

    public void code(String query) {
        persistRequest(query);
        var roleRequest = UserRoles.codePrompt(query);
        var systemMsg = buildMessage(roleRequest, query);
        chatRoleBuilder(systemMsg, query);
        persistResponse("");
    }

    protected abstract String chatBuilder(Gigachatv1.ChatRequest request);
    protected abstract String chatRoleBuilder(Gigachatv1.Message systemMsg, String query);
}
