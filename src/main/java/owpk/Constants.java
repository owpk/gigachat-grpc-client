package owpk;

import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class Constants {
    public static final String USER_ROLE = "user";
    public static final String CHAT_ROLE = "system";

    public static final String BEARER_TYPE = "Bearer";
    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);

    public static class GigachatScope {
        public static final String PERSONAL = "GIGACHAT_API_PERS";
    }
}
