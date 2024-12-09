package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import owpk.properties.concrete.CredentialProps;
import owpk.role.RolePrompt;

@Slf4j
public class RetryingChatWrapper implements ChatService {
    private final CredentialProps credentialProps;
    private final AuthorizationRestService authorizationRestService;
    private final ChatService delegate;

    public RetryingChatWrapper(CredentialProps credentialProps,
                               ChatService delegate,
                               AuthorizationRestService authorizationRestService) {
        this.credentialProps = credentialProps;
        this.delegate = delegate;
        this.authorizationRestService = authorizationRestService;
    }

    public void setUnaryMode() {
        delegate.setUnaryMode();
    }

    public void setStreamMode() {
        delegate.setStreamMode();
    }

    @Override
    public void chat(RolePrompt rolePrompt) {
        catchUnauthorized(() -> delegate.chat(rolePrompt));
    }

    private void catchUnauthorized(Runnable callable) {
        try {
            callable.run();
        } catch (StatusRuntimeException e) {
            log.info("Catch grpc status exception: " + e.getStatus());
            if (e.getStatus().getCode().equals(Status.UNAUTHENTICATED.getCode())) {
                log.info("Seems jwt token is corrupted. Refreshing token...");
                try {
                    authorizationRestService.refreshToken();
                    callable.run();
                } catch (Exception ex) {
                    credentialProps.setProperty(CredentialProps.DEF_COMPOSED_CREDENTIALS, "");
                    throw new RuntimeException("Error while refreshing token", ex);
                }
            } else throw e;
        } catch (Throwable e) {
            log.error("Catch retrying exception: " + e);
        }
    }
}
