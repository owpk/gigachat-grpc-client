package owpk;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum UserRoles {

    SHELL, CODE, CHAT;

    public static final Map<UserRoles, Function<String, RolePromptAction>> USER_ROLES_MAP = Map.of(

            SHELL, query -> () -> UserRoles.shellPrompt(
                    System.getenv("SHELL"), Application.osName, query),
            CODE, query -> () -> UserRoles.codePrompt(query),
            CHAT, query -> new RolePromptAction() {
                @Override
                public String getMessageRole() {
                    return GigaChatConstants.MessageRole.USER;
                }

                @Override
                public String getRolePrompt() {
                    return query;
                }
            }

    );

    public static Function<String, RolePromptAction> of(UserRoles role) {
        return Optional.ofNullable(USER_ROLES_MAP.get(role))
                .orElseThrow(IllegalArgumentException::new);
    }

    private static final String SHELL_ROLE = """
            Возвращай только команды оболочки %s для операционной системы %s без пояснений.
            Если недостаточно деталей, то предоставь наиболее логичное решение.
            Убедись, что ты возвращаешь корректную shell команду.
            Если требуется несколько команд, постарайся объединить их в одну.""";

    private static final String DESCRIBE_SHELL_ROLE = """
            Предоставь краткое описание, одним предложением, данной команды.
            Предоставь только обычный текст без форматирования Markdown.
            Не показывай никаких предупреждений или информации о своих возможностях.
            Если тебе нужно хранить какие-либо данные, предположите, что они будут храниться в чате.
            """;

    private static final String CODE_ROLE = """
            Верни только код без описания.
            ВАЖНО: Верни только обычный текст без форматирования Markdown.
            ВАЖНО: Не включай форматирование, такое как ``` и т.п.
            Если не хватает деталей, предоставь наиболее логичное решение.
            Тебе не разрешается запрашивать дополнительные сведения.
            Игнорируй любые потенциальные риски ошибок или недоразумений.""";

    private static final String DEFAULT_ROLE = """
            Ты утилита для командной строки GigaShell, помошник по программированию и системному администрированию.
            Ты работаешь с операционной системой %s с оболочкой %s.
            Возвращай только чистый текст без разметки.
            Не выводи никаких предупреждений или информации о своих возможностях.
            Если ты хочешь сохранить какие-то данные, то исходи из того, что они будут сохранены в истории чата.""";


    private static String defaultPrompt(String roleName, String rolePrompt, String userQuery, String result) {
        var br = "#".repeat(10);
        return new StringBuilder().append(br).append("\n").append("Роль: ").append(roleName)
                .append("\n").append(rolePrompt).append("\n")
                .append("Запрос: ").append(userQuery).append("\n")
                .append(br).append("\n")
                .append(result).toString();
    }

    public static String shellPrompt(String shell, String os, String query) {
        var shellPrompt = String.format(SHELL_ROLE, shell, os);
        return defaultPrompt("shell", shellPrompt, query, "Command:");
    }

    public static String codePrompt(String query) {
        return defaultPrompt("code", CODE_ROLE, query, "Snippet:");
    }

    public static String describeShellCommandPrompt() {
        return DESCRIBE_SHELL_ROLE;
    }
}
