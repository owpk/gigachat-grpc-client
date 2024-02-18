package owpk.grpc;

import owpk.JwtRestResponse;

public interface JwtTokenProvider {
    String getJwt();

    JwtRestResponse refreshToken();
}
