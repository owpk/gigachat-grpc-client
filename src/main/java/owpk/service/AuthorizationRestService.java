package owpk.service;

import owpk.Constants;
import owpk.JwtRestResponse;
import owpk.api.AuthRestClient;
import owpk.config.AppSettings;
import owpk.grpc.JwtTokenProvider;
import owpk.storage.SettingsStore;

import java.util.Date;

public class AuthorizationRestService implements JwtTokenProvider {
    private final AuthRestClient client;
    private final AppSettings settings;
    private final SettingsStore settingsStore = SettingsStore.INSTANCE;
    private final String credentialsHash;

    public AuthorizationRestService(AuthRestClient client, AppSettings settings) {
        this.client = client;
        this.settings = settings;
        credentialsHash = settings.getComposedCredentials();
    }

    @Override
    public String getJwt() {
        var currentJwt = settings.getJwt();
        if (currentJwt != null && currentJwt.getAccessToken() != null
                && validateExpiration(currentJwt.getExpiresAt())) {
            return currentJwt.getAccessToken();
        }
        return refreshToken().getAccessToken();
    }

    @Override
    public JwtRestResponse refreshToken() {
        var jwt = client.authorize(Constants.GigachatScope.PERSONAL, credentialsHash);
        System.out.println("Retrieving jwt from giga api...");
        System.out.println("jwt: " + jwt);
        rewriteJwt(jwt);
        return jwt;
    }

    public void rewriteJwt(JwtRestResponse jwt) {
        System.out.println("SETTINGS STORE: " + settingsStore);
        settingsStore.setProperty("gigachat.jwt.accessToken", jwt.getAccessToken());
        settingsStore.setProperty("gigachat.jwt.expiresAt", jwt.getExpiresAt().toString());
        System.out.println("SETTINGS: " + settings);
        settings.getJwt().setAccessToken(jwt.getAccessToken());
        settings.getJwt().setExpiresAt(jwt.getExpiresAt());
    }

    private boolean validateExpiration(Long expiresAt) {
        if (expiresAt != null && expiresAt != 0) {
            var currentTime = new Date().getTime();
            return currentTime < expiresAt;
        }
        return false;
    }
}
