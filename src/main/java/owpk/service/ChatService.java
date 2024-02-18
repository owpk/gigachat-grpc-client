package owpk.service;

import gigachat.v1.Gigachatv1;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.util.stream.Collectors;

@Singleton
public class ChatService {

    private final GigaChatGRpcClient gigaChatGRpcClient;

    @Inject
    public ChatService(GigaChatGRpcClient gigaChatGRpcClient) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        SettingsStore.validate();
    }

    public void chat(String query) {
        var response = gigaChatGRpcClient.chat(buildRequest(query));
        response.getAlternativesList().forEach(a -> System.out.print(a.getMessage().getContent()));
    }

    public void chatStream(String query) {
        var iter = gigaChatGRpcClient.chatStream(buildRequest(query));
        while (iter.hasNext()) {
            var msg = iter.next();
            System.out.print(msg.getAlternativesList().stream().map(a -> a.getMessage().getContent())
                    .collect(Collectors.joining(" ")));
        }
    }

    private Gigachatv1.ChatRequest buildRequest(String query) {
        return Gigachatv1.ChatRequest.newBuilder()
                .setModel("GigaChat:latest")
                .addMessages(Gigachatv1.Message.newBuilder()
                        .setRole("user")
                        .setContent(query)
                        .build())
                .build();
    }

}
