package owpk.config;

import com.squareup.okhttp.OkHttpClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.api.AuthRestClient;
import owpk.api.AuthRestClientImpl;
import owpk.grpc.GigaChatGRpcClient;
import owpk.service.*;
import owpk.storage.app.MainSettingsStore;
import owpk.storage.app.RolesStorage;
import picocli.CommandLine;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static owpk.Application.showApiDocsHelp;

@Factory
@Slf4j
public class BeanFactory {

    @Singleton
    @Order(value = Integer.MAX_VALUE)
    public MainSettingsStore settingsStore() {
        var settings = new MainSettingsStore();
        settings.validate(() -> {
            System.out.println(CommandLine.Help.Ansi.AUTO.string(
                    "@|bold,fg(yellow) Specify your credentials!|@"));
            showApiDocsHelp();
            System.out.print("Input 'Basic' auth: ");
        });
        return settings;
    }

    @Singleton
    public RolesStorage rolesStorage() {
        var rolesStore = new RolesStorage();
        rolesStore.validate(() -> {});
        return rolesStore;
    }

    @Singleton
    public OkHttpClient okHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final var sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getSslCerts(), new java.security.SecureRandom());
            final var sslSocketFactory = sslContext.getSocketFactory();

            var client = new OkHttpClient();
            client.setSslSocketFactory(sslSocketFactory);
            client.setHostnameVerifier((hostname, session) -> true);
            return client;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TrustManager[] getSslCerts() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };
    }

    @Singleton
    public AuthRestClient authRestClient(OkHttpClient okHttpClient, MainSettingsStore settings) {
        return new AuthRestClientImpl(settings, okHttpClient);
    }

    @Singleton
    public AuthorizationRestService authorizationRestService(AuthRestClient authRestClient, MainSettingsStore mainSettingsStore) {
        return new AuthorizationRestService(authRestClient, mainSettingsStore);
    }

    @Singleton
    public GigaChatGRpcClient gRpcClient(AuthorizationRestService authorizationRestService, MainSettingsStore settings) throws SSLException {
        return new GigaChatGRpcClient(settings.getSettings().getTarget(), authorizationRestService);
    }

    @Singleton
    public ChatService chatService(GigaChatGRpcClient gRpcClient,
                                   ChatHistoryService historyService,
                                   MainSettingsStore mainSettingsStore) {
        return new ChatServiceImpl(gRpcClient, historyService, mainSettingsStore);
    }

    @Singleton
    public ChatHistoryService chatHistoryService(MainSettingsStore mainSettingsStore) {
       return new ChatHistoryService(mainSettingsStore);
    }

    @Singleton
    public RetryingChatWrapper retryingChatWrapper(MainSettingsStore mainSettingsStore, ChatService chatService,
                                                   AuthorizationRestService authorizationRestService) {
        return new RetryingChatWrapper(mainSettingsStore, chatService, authorizationRestService);
    }
}
