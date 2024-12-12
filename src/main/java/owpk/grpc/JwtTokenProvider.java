package owpk.grpc;

import owpk.model.JwtRestResponse;

public interface JwtTokenProvider {
    String getJwt();

    JwtRestResponse refreshToken();

    void cleareToken();
}
