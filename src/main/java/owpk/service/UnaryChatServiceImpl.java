package owpk.service;

import gigachat.v1.Gigachatv1;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

@Slf4j
@Singleton
public class UnaryChatServiceImpl extends DefaultAbsChat {

    @Inject
    public UnaryChatServiceImpl(GigaChatGRpcClient gigaChatGRpcClient,
                                ChatHistoryService chatHistoryService,
                                SettingsStore settingsStore) {
        super(gigaChatGRpcClient, chatHistoryService, settingsStore);
    }

    @Override
    protected String chatRoleBuilder(Gigachatv1.Message systemMsg, String query) {
        return null;
    }

    @Override
    public void shell(String query) {
    }

    @Override
    public void code(String query) {
    }

    @Override
    protected String handleChatRequest(Gigachatv1.ChatRequest request) {
        var response = gigaChatGRpcClient.chat(request);
        var content = defaultHandleResponse(response);
        System.out.println(content);
        return content;
    }

}
