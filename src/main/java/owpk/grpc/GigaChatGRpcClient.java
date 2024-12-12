package owpk.grpc;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import gigachat.v1.ChatServiceGrpc;
import gigachat.v1.Gigachatv1;
import gigachat.v1.ModelsServiceGrpc;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import owpk.properties.concrete.MainProps;


@Slf4j
public class GigaChatGRpcClient extends ModelsServiceGrpc.ModelsServiceImplBase {
    private static final Integer DEFAULT_SSL_PORT = 443;
    @Getter
    private final JwtTokenProvider jwtTokenProvider;
    private final ChatServiceGrpc.ChatServiceBlockingStub stub;
    private final ModelsServiceGrpc.ModelsServiceBlockingStub modelStub;
    private final CallCredentials callCredentials;

    public GigaChatGRpcClient(MainProps props, JwtTokenProvider jwtTokenProvider) throws SSLException {
        this.jwtTokenProvider = jwtTokenProvider;
        var channel = configureNettyChannel(props.getProperty(MainProps.DEF_TARGET));
        callCredentials = new GigaChatCreds(jwtTokenProvider);
        stub = ChatServiceGrpc.newBlockingStub(channel).withCallCredentials(callCredentials);
        modelStub = ModelsServiceGrpc.newBlockingStub(channel).withCallCredentials(callCredentials);
    }

    private Channel configureNettyChannel(String target) throws SSLException {
        var ssl = GrpcSslContexts.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        var builder = NettyChannelBuilder.forAddress(target, DEFAULT_SSL_PORT)
                .enableRetry()
                .keepAliveTime(10, TimeUnit.SECONDS);
        return builder
                .sslContext(ssl)
                .build();
    }

    public Gigachatv1.ListModelsResponse modelList() {
        return modelStub.listModels(Gigachatv1.ListModelsRequest.newBuilder().build());
    }

    public Gigachatv1.ChatResponse chat(Gigachatv1.ChatRequest chatRequest) {
        return catchUnauthorized(() -> stub.chat(chatRequest));
    }

    public Iterator<Gigachatv1.ChatResponse> chatStream(Gigachatv1.ChatRequest chatRequest) {
        return catchUnauthorized(() -> stub.chatStream(chatRequest));
    }

    private <T> T catchUnauthorized(Callable<T> callable) {
        try {
            return callable.call();
        } catch (StatusRuntimeException e) {
            log.info("Catch grpc status exception: " + e.getStatus());
            if (e.getStatus().getCode().equals(Status.UNAUTHENTICATED.getCode())) {
                log.info("Seems jwt token is corrupted. Refreshing token...");
                try {
                    jwtTokenProvider.refreshToken();
                    return callable.call();
                } catch (Exception ex) {
                    jwtTokenProvider.cleareToken();
                    throw new RuntimeException("Error while refreshing token", ex);
                }
            } else throw e;
        } catch (Throwable e) {
            log.error("Catch retrying exception: " + e);
            throw new RuntimeException(e);
        }
    }
}
