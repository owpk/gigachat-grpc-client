package owpk.grpc;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;

import static owpk.Constants.AUTHORIZATION_METADATA_KEY;
import static owpk.Constants.BEARER_TYPE;

@Slf4j
public class GigaChatCreds extends CallCredentials {
    private final JwtTokenProvider tokenProvider;

    public GigaChatCreds(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
        log.info("Executing request: {}", requestInfo.getMethodDescriptor().getBareMethodName());
        try {
            var jwt = tokenProvider.getJwt();
            doRequest(applier, jwt);
        } catch (Throwable e) {
            applier.fail(Status.UNAUTHENTICATED);
        }
    }

    private void doRequest(MetadataApplier applier, String jwt) {
        var headers = new Metadata();
        headers.put(AUTHORIZATION_METADATA_KEY,
                String.format("%s %s", BEARER_TYPE, jwt));
        applier.apply(headers);
    }

}
