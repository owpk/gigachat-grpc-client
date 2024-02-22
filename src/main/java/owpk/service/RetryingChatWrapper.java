package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micronaut.context.ApplicationContext;
import lombok.extern.slf4j.Slf4j;
import owpk.model.PromptRole;
import owpk.storage.SettingsStore;

// TODO search for aop/proxy solution
@Slf4j
public class RetryingChatWrapper implements ChatService {
    private final SettingsStore settingsStore;
    private final AuthorizationRestService authorizationRestService;
    private final ChatService delegate;

    public RetryingChatWrapper(SettingsStore settingsStore,
                               ChatService delegate,
                               AuthorizationRestService authorizationRestService) {
        this.settingsStore = settingsStore;
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
    public void chat(PromptRole promptRole, int lastMessageCount) {
        catchUnauthorized(() -> delegate.chat(promptRole, lastMessageCount));
    }

    @Override
    public void chat(PromptRole promptRole) {
        catchUnauthorized(() -> delegate.chat(promptRole));
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
                    settingsStore.setProperty("gigachat.composedCredentials", "");
                    throw new RuntimeException("Error while refreshing token", ex);
                }
            } else throw e;
        } catch (Throwable e) {
            log.error("Catch retrying exception: " + e);
        }
    }
}
