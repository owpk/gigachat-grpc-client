package owpk.api;

import owpk.model.JwtRestResponse;

public interface AuthRestClient {

    JwtRestResponse authorize(String scope,
                              String basicAuth

    );
}
