package owpk.config;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.squareup.okhttp.OkHttpClient;

import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.api.AuthRestClient;
import owpk.api.AuthRestClientImpl;
import owpk.cli.ConsoleWriter;
import owpk.grpc.GigaChatGRpcClient;
import owpk.properties.BootstrapPropertiesFactory;
import owpk.properties.concrete.CredentialProps;
import owpk.properties.concrete.MainProps;
import owpk.role.RolesService;
import owpk.service.AuthorizationRestService;
import owpk.service.ChatHistoryService;
import owpk.service.ChatService;
import owpk.service.ChatServiceImpl;

@Factory
@Slf4j
public class BeanFactory {

    @Singleton
    @Order(value = Integer.MAX_VALUE)
    public MainProps mainProps() {
        return (MainProps) BootstrapPropertiesFactory.getInstance()
            .getProvider(MainProps.class);
    }

    @Singleton
    @Order(value = Integer.MAX_VALUE - 1)
    public CredentialProps credentialProps() {
        return (CredentialProps) BootstrapPropertiesFactory.getInstance()
            .getProvider(CredentialProps.class);
    }

    @Singleton
    public RolesService rolesStorage(MainProps props) {
        return new RolesService(mainProps().getStorage(), props);
    }

    @Singleton
    public ChatHistoryService chatHistoryService(MainProps props) {
       return new ChatHistoryService(props.getStorage(), props);
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
    public AuthRestClient authRestClient(OkHttpClient okHttpClient, MainProps settings) {
        return new AuthRestClientImpl(settings, okHttpClient);
    }

    @Singleton
    public AuthorizationRestService authorizationRestService(AuthRestClient authRestClient, CredentialProps credsProps) {
        return new AuthorizationRestService(authRestClient, credsProps);
    }

    @Singleton
    public GigaChatGRpcClient gRpcClient(AuthorizationRestService authorizationRestService, MainProps settings) throws SSLException {
        return new GigaChatGRpcClient(settings, authorizationRestService);
    }

    @Singleton
    public ChatService chatService(GigaChatGRpcClient gRpcClient,
                                   ChatHistoryService historyService,
                                   MainProps mainSettingsStore) {
        return new ChatServiceImpl(out -> ConsoleWriter.write(out), out -> ConsoleWriter.writeLn(out),
            gRpcClient, historyService, mainSettingsStore);
    }
}
