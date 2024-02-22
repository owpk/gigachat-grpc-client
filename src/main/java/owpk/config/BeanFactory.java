package owpk.config;

import com.squareup.okhttp.OkHttpClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.api.AuthRestClient;
import owpk.api.AuthRestClientImpl;
import owpk.grpc.GigaChatGRpcClient;
import owpk.service.*;
import owpk.storage.SettingsStore;
import picocli.CommandLine;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static owpk.Application.showApiDocsHelp;

@Factory
@Slf4j
public class BeanFactory {
    private final AppSettings settings;

    @Inject
    public BeanFactory(AppSettings settings) {
        this.settings = settings;
    }

    @Singleton
    @Order(value = Integer.MAX_VALUE)
    public SettingsStore settingsStore() {
        var settings = new SettingsStore(this.settings);
        settings.init();
        settings.validate(() -> {
            System.out.println(CommandLine.Help.Ansi.AUTO.string(
                    "@|bold,fg(yellow) Specify your credentials!|@"));
            showApiDocsHelp();
            System.out.print("Input 'Basic' auth: ");
        });
        return settings;
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
    public AuthRestClient authRestClient(OkHttpClient okHttpClient) {
        return new AuthRestClientImpl(settings, okHttpClient);
    }

    @Singleton
    public AuthorizationRestService authorizationRestService(AuthRestClient authRestClient, SettingsStore settingsStore) {
        return new AuthorizationRestService(authRestClient, settingsStore);
    }

    @Singleton
    public GigaChatGRpcClient gRpcClient(AuthorizationRestService authorizationRestService) throws SSLException {
        return new GigaChatGRpcClient(settings.getTarget(), authorizationRestService);
    }

    @Singleton
    public ChatService chatService(GigaChatGRpcClient gRpcClient,
                                   ChatHistoryService historyService,
                                   SettingsStore settingsStore) {
        return new ChatServiceImpl(gRpcClient, historyService, settingsStore);
    }

    @Singleton
    public RetryingChatWrapper retryingChatWrapper(SettingsStore settingsStore, ChatService chatService,
                                                   AuthorizationRestService authorizationRestService) {
        return new RetryingChatWrapper(settingsStore, chatService, authorizationRestService);
    }
}
