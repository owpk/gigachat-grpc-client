package owpk.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import owpk.JwtRestResponse;
import owpk.config.AppSettings;

import java.util.UUID;

public class AuthRestClientImpl implements AuthRestClient {
    private final AppSettings settings;
    private final OkHttpClient okHttp;
    //    private final UnirestInstance unirest;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthRestClientImpl(AppSettings settings, OkHttpClient okHttp) {
        this.settings = settings;
        this.okHttp = okHttp;
        init();
    }

    private void init() {

    }

    @Override
    public JwtRestResponse authorize(String scope, String basicAuth) {
        try {
            var body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "scope=" + scope);
            var request = new Request.Builder()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .header("RqUID", UUID.randomUUID().toString())
                    .header("Authorization", "Basic " + basicAuth)
                    .post(body)
                    .url(settings.getAuthUri())
                    .build();

            var response = okHttp.newCall(request).execute();

            // this cringe is needed to make native image build works.....
            if (response.isSuccessful()) {
                try (var rawBody = response.body()) {
                    var bytes = rawBody.bytes();
                    JsonNode node = mapper.readTree(bytes);
                    var accessToken = node.get("access_token").asText();
                    var expiresAt = node.get("expires_at").asLong();
                    return new JwtRestResponse(accessToken, expiresAt);
                }
            }
            throw new RuntimeException("Can't authorize: " + response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
