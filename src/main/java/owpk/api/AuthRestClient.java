package owpk.api;

import owpk.JwtRestResponse;

public interface AuthRestClient {

    JwtRestResponse authorize(String scope,
                              String basicAuth

    );
}
