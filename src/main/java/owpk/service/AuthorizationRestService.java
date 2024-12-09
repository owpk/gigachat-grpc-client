package owpk.service;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import owpk.GigaChatConstants;
import owpk.api.AuthRestClient;
import owpk.grpc.JwtTokenProvider;
import owpk.model.JwtRestResponse;
import owpk.properties.concrete.CredentialProps;

@Slf4j
public class AuthorizationRestService implements JwtTokenProvider {
    private final AuthRestClient client;
    private final CredentialProps credentialProps;

    public AuthorizationRestService(AuthRestClient client, CredentialProps credsProps) {
        this.client = client;
        this.credentialProps = credsProps;
    }

    @Override
    public String getJwt() {
        log.info("JWT: Attempt to retrieve jwt token...");

        var currentJwt = credentialProps.getJwt();
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
        var jwt = client.authorize(GigaChatConstants.Scope.PERSONAL, credentialProps.getComposedCredentials());
        rewriteJwt(jwt);
        return jwt;
    }

    public void rewriteJwt(JwtRestResponse jwt) {
        credentialProps.rewriteJwt(jwt);
    }

    private boolean validateExpiration(Long expiresAt) {
        if (expiresAt != null && expiresAt != 0) {
            var currentTime = new Date().getTime();
            return currentTime < expiresAt;
        }
        return false;
    }
}
