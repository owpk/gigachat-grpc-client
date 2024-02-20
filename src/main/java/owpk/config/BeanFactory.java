package owpk.config;

import com.squareup.okhttp.OkHttpClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Order;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import owpk.api.AuthRestClient;
import owpk.api.AuthRestClientImpl;
import owpk.grpc.GigaChatGRpcClient;
import owpk.service.AuthorizationRestService;
import owpk.storage.SettingsStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;

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
        var bean = new SettingsStore();
        try {
            bean.init();
            bean.setDefaults();
        } catch (Exception e) {
            log.error("Error while init settings.", e);
            throw new RuntimeException(e);
        }
        return bean;
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
                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
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
        return new AuthorizationRestService(authRestClient, settings, settingsStore);
    }

    @Singleton
    public GigaChatGRpcClient gRpcClient(AuthorizationRestService authorizationRestService) throws SSLException {
        return new GigaChatGRpcClient(settings.getTarget(), authorizationRestService);
    }

}
