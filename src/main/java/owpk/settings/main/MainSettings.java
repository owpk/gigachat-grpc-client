package owpk.settings.main;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Introspected
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class MainSettings {
    String target;
    String model;
    String composedCredentials;
    String authUri;
    String currentChat;
    Jwt jwt;

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    @Introspected
    public static class Jwt {
        String accessToken;
        Long expiresAt;
    }
}