package owpk.service;

import lombok.extern.slf4j.Slf4j;
import owpk.Constants;
import owpk.JwtRestResponse;
import owpk.api.AuthRestClient;
import owpk.config.AppSettings;
import owpk.grpc.JwtTokenProvider;
import owpk.storage.SettingsStore;

import java.util.Date;

@Slf4j
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
        log.info("JWT: Attempt to retrieve jwt token...");
        var currentJwt = settings.getJwt();
        if (currentJwt != null && currentJwt.getAccessToken() != null
                && validateExpiration(currentJwt.getExpiresAt())) {
            log.info("JWT: Getting jwt from cache: " + currentJwt);
            return currentJwt.getAccessToken();
        }
        log.info("JWT: Jwt not valid. Retrieving jwt from api...");
        return refreshToken().getAccessToken();
    }

    @Override
    public JwtRestResponse refreshToken() {
        log.info("JWT: Attempting to refresh token...");
        var jwt = client.authorize(Constants.GigachatScope.PERSONAL, credentialsHash);
        rewriteJwt(jwt);
        return jwt;
    }

    public void rewriteJwt(JwtRestResponse jwt) {
        settingsStore.setProperty("gigachat.jwt.accessToken", jwt.getAccessToken());
        settingsStore.setProperty("gigachat.jwt.expiresAt", jwt.getExpiresAt().toString());
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
