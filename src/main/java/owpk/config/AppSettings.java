package owpk.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;
import lombok.NoArgsConstructor;

@Context
@ConfigurationProperties(value = "gigachat")
@Data
@NoArgsConstructor
@Introspected
public class AppSettings {
    String target;
    String model;
    String composedCredentials;
    String authUri;
    Jwt jwt;

    @ConfigurationProperties("jwt")
    @Data
    @NoArgsConstructor
    public static class Jwt {
        String accessToken;
        Long expiresAt;
    }
}