package owpk;

import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class GigaChatConstants {
    public static final String BEARER_TYPE = "Bearer";
    public static final Metadata.Key<String> AUTHORIZATION_METADATA_KEY = Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER);

    public static class Scope {
        public static final String PERSONAL = "GIGACHAT_API_PERS";
    }

    public static class Role {

        // сообщение пользователя;
        public static final String USER = "user";

        // системный промпт, который задает роль модели, например,
        // должна модель отвечать как академик или как школьник;
        public static final String SYSTEM = "system";

        // ответ модели;
        public static final String ASSISTANT = "assistant";

        // позволяет передать модели документ,
        // который она должна использовать для генерации ответов.
        // Используется для поддержки RAG.
        public static final String SEARCH_RESULT = "search_result";

    }
}
