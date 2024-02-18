package owpk.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

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
            if (e.getStatus().getCode().equals(Status.UNAUTHENTICATED.getCode())) {
                System.out.println("STATUS EXCEPTION: " + e.getMessage());
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
