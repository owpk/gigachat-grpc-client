package owpk;

import io.grpc.Metadata;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public final class GigaChatConstants {
    public static final String BEARER_TYPE = "Bearer";
    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);

    public static class Scope {
        public static final String PERSONAL = "GIGACHAT_API_PERS";
    }

    @Getter
    public enum MessageRole {
        USER("user"),
        SYSTEM("system"),
        ASSISTANT("assistant"),
        SEARCH_RESULT("search_result");
        // сообщение пользователя;

        private final String value;

        MessageRole(String value) {
            this.value = value;
        }

        public static final Map<String, MessageRole> messageRoleMap = Arrays.stream(MessageRole.values())
                .collect(Collectors.toMap(it -> it.value, Function.identity()));

        public static MessageRole of(String value) {
            return messageRoleMap.get(value);
        }
    }
}
