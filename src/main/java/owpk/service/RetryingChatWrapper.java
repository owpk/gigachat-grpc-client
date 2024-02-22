package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import owpk.storage.SettingsStore;

@Slf4j
public class RetryingChatWrapper {
    private final ChatService delegate;
    private final SettingsStore settingsStore;
    private final AuthorizationRestService authorizationRestService;

    public RetryingChatWrapper(ChatService delegate, SettingsStore settingsStore,
                               AuthorizationRestService authorizationRestService) {
        this.delegate = delegate;
        this.settingsStore = settingsStore;
        this.authorizationRestService = authorizationRestService;
    }

    public void chat(String query) {
        catchUnauthorized(() -> delegate.chat(query));
    }

    public void chatStream(String query) {
        catchUnauthorized(() -> delegate.chatStream(query));
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
