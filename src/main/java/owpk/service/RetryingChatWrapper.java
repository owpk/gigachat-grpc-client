package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import owpk.role.RolePrompt;
import owpk.settings.main.MainSettingField;
import owpk.storage.app.MainSettingsStore;

// TODO search for aop/proxy solution
@Slf4j
public class RetryingChatWrapper implements ChatService {
    private final MainSettingsStore mainSettingsStore;
    private final AuthorizationRestService authorizationRestService;
    private final ChatService delegate;

    public RetryingChatWrapper(MainSettingsStore mainSettingsStore,
                               ChatService delegate,
                               AuthorizationRestService authorizationRestService) {
        this.mainSettingsStore = mainSettingsStore;
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
                    mainSettingsStore.setProperty(MainSettingField.COMPOSED_CREDENTIALS.getPropertyKey(), "");
                    throw new RuntimeException("Error while refreshing token", ex);
                }
            } else throw e;
        } catch (Throwable e) {
            log.error("Catch retrying exception: " + e);
        }
    }
}
