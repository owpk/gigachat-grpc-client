package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryingChatWrapper {
    private final ChatService delegate;
    private final AuthorizationRestService authorizationRestService;

    public RetryingChatWrapper(ChatService delegate, AuthorizationRestService authorizationRestService) {
        this.delegate = delegate;
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
                try {
                    authorizationRestService.refreshToken();
                    callable.run();
                } catch (Exception ex) {
                    throw new RuntimeException("Error while refreshing token", ex);
                }
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
