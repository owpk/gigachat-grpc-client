package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Qualifier;
import lombok.extern.slf4j.Slf4j;
import owpk.storage.SettingsStore;

@Slf4j
public class RetryingChatWrapper implements ChatService {
    private final SettingsStore settingsStore;
    private final AuthorizationRestService authorizationRestService;
    private final ApplicationContext applicationContext;
    private ChatService delegate;

    public RetryingChatWrapper(SettingsStore settingsStore,
                               AuthorizationRestService authorizationRestService,
                               ApplicationContext applicationContext) {
        this.settingsStore = settingsStore;
        this.authorizationRestService = authorizationRestService;
        this.applicationContext = applicationContext;
    }

    public void setUnaryMode() {
        this.delegate = applicationContext.getBean(UnaryChatServiceImpl.class);
    }

    public void setStreamMode() {
        this.delegate = applicationContext.getBean(StreamChatServiceImpl.class);
    }

    public void chat(String query, int lastMessageCount) {
        catchUnauthorized(() -> delegate.chat(query, lastMessageCount));
    }

    @Override
    public void shell(String query) {
        catchUnauthorized(() -> delegate.shell(query));
    }

    @Override
    public void code(String query) {
        catchUnauthorized(() -> delegate.code(query));
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
