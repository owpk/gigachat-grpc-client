package owpk.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Context
@ConfigurationProperties(value = "gigachat")
@Data
@NoArgsConstructor
@Introspected
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AppSettings {
    String target;
    String model;
    String composedCredentials;
    String authUri;
    Jwt jwt;

    @ConfigurationProperties("jwt")
    @Data
    @NoArgsConstructor
    @FieldDefaults(level = lombok.AccessLevel.PRIVATE)
    public static class Jwt {
        String accessToken;
        Long expiresAt;
    }
}