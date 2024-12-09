package owpk.service.impl;

import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.api.AuthRestClient;
import owpk.grpc.JwtTokenProvider;
import owpk.model.JwtRestResponse;
import owpk.settings.main.MainSettings;
import owpk.storage.app.MainSettingsStore;

import java.util.Date;

@Slf4j
public class AuthorizationRestService implements JwtTokenProvider {
    private final AuthRestClient client;
    private final MainSettings settings;
    private final MainSettingsStore mainSettingsStore;

    public AuthorizationRestService(AuthRestClient client, MainSettingsStore mainSettingsStore) {
        this.client = client;
        this.settings = mainSettingsStore.getSettings();
        this.mainSettingsStore = mainSettingsStore;
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
        var jwt = client.authorize(GigaChatConstants.Scope.PERSONAL, settings.getComposedCredentials());
        rewriteJwt(jwt);
        return jwt;
    }

    public void rewriteJwt(JwtRestResponse jwt) {
        mainSettingsStore.setProperty("gigachat.jwt.accessToken", jwt.getAccessToken());
        mainSettingsStore.setProperty("gigachat.jwt.expiresAt", jwt.getExpiresAt().toString());
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
