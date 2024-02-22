package owpk.service;

import gigachat.v1.Gigachatv1;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.io.StringWriter;
import java.util.stream.Collectors;

@Slf4j
@Singleton
// TODO refactor: move chat history logic to its own class
public class ChatService {
    private final GigaChatGRpcClient gigaChatGRpcClient;
    private final SettingsStore settingsStore;
    private final ChatHistoryService chatHistoryService;

    @Inject
    public ChatService(GigaChatGRpcClient gigaChatGRpcClient,
                       ChatHistoryService chatHistoryService,
                       SettingsStore settingsStore) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        this.chatHistoryService = chatHistoryService;
        this.settingsStore = settingsStore;
    }

    public void chat(String query) {
        var response = gigaChatGRpcClient.chat(buildRequest(query));
        response.getAlternativesList().forEach(a -> System.out.print(a.getMessage().getContent()));
    }

    public void chatStream(String query) {
        var iterator = gigaChatGRpcClient.chatStream(buildRequest(query));
        var sw = new StringWriter();
        while (iterator.hasNext()) {
            var msg = iterator.next();
            var content = msg.getAlternativesList().stream().map(a -> a.getMessage().getContent())
                    .collect(Collectors.joining(" "));
            sw.append(content);
            System.out.print(content);
        }

        chatHistoryService.persistContentToHistory(sw.toString(), GigaChatConstants.Role.ASSISTANT);
    }

    private Gigachatv1.ChatRequest buildRequest(String query) {
        var lastMessages = chatHistoryService.readLastMessages(5)
                .stream().map(it -> buildMessage(it.content(), it.role()))
                .collect(Collectors.toList());

        chatHistoryService.persistContentToHistory(query, GigaChatConstants.Role.USER);

        log.info("""
                Building grpc request:
                    Query: {}
                    Last messages count: {}
                    Last messages roles: {}
                """, query, lastMessages.size(), lastMessages.stream()
                .map(Gigachatv1.Message::getRole)
                .collect(Collectors.joining(" ")));

        return Gigachatv1.ChatRequest.newBuilder()
                .setModel(settingsStore.getAppSettings().getModel())
                .addAllMessages(lastMessages)
                .addMessages(Gigachatv1.Message.newBuilder()
                        .setRole(GigaChatConstants.Role.USER)
                        .setContent(query)
                        .build())
                .build();
    }

    private Gigachatv1.Message buildMessage(String content, String role) {
        return Gigachatv1.Message.newBuilder()
                .setRole(role)
                .setContent(content)
                .build();
    }
}
