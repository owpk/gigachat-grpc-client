package owpk.service;

import gigachat.v1.Gigachatv1;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.io.StringWriter;

@Singleton
public class StreamChatServiceImpl extends DefaultAbsChat {

    public StreamChatServiceImpl(GigaChatGRpcClient gigaChatGRpcClient,
                                 ChatHistoryService chatHistoryService,
                                 SettingsStore settingsStore) {
        super(gigaChatGRpcClient, chatHistoryService, settingsStore);
    }

    @Override
    public void shell(String query) {
    }

    @Override
    public void code(String query) {
    }

    @Override
    public String handleChatRequest(Gigachatv1.ChatRequest chatRequest) {
        return defaultStream(chatRequest);
    }

    private String defaultStream(Gigachatv1.ChatRequest request) {
        var sw = new StringWriter();
        var iterator = gigaChatGRpcClient.chatStream(request);
        while (iterator.hasNext()) {
            var msg = iterator.next();
            var content = defaultHandleResponse(msg);
            sw.append(content);
            System.out.print(content);
        }
        return sw.toString();
    }

    @Override
    protected String chatRoleBuilder(Gigachatv1.Message systemMsg, String query) {
        return null;
    }

}
