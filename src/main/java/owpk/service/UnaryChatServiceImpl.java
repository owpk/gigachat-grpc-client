package owpk.service;

import gigachat.v1.Gigachatv1;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.grpc.GigaChatGRpcClient;
import owpk.storage.SettingsStore;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.stream.Collectors;

@Slf4j
@Singleton
// TODO return data from methods, print result in console
//  !!! No need for now !!!
@Named(value = "chat_unary")
public class UnaryChatServiceImpl extends DefaultAbsChat {

    @Inject
    public UnaryChatServiceImpl(GigaChatGRpcClient gigaChatGRpcClient,
                                ChatHistoryService chatHistoryService,
                                SettingsStore settingsStore) {
        super(gigaChatGRpcClient, chatHistoryService, settingsStore);
    }

    @Override
    public String chatBuilder(String query, int lastMessageCount) {
        var responseMsg = gigaChatGRpcClient.chat(buildRequest(query, lastMessageCount));
        return handleResponse(responseMsg);
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


}
