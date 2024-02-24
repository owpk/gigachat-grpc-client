package owpk;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public enum ChatRoles {

    SHELL, CODE, CHAT, DESCRIBE_SHELL;

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

    public static final Map<ChatRoles, Function<String, RolePromptAction>> USER_ROLES_MAP = Map.of(
            DESCRIBE_SHELL, query -> () -> ChatRoles.describeShellCommandPrompt(query),
            SHELL, query -> () -> ChatRoles.shellPrompt(
                    System.getenv("SHELL"), Application.osName, query),
            CODE, query -> () -> ChatRoles.codePrompt(query),
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
    private static final String DEFAULT_SHELL_ROLE = """
            Ты утилита для командной строки GigaShell, помошник по программированию и системному администрированию.
            Ты работаешь с операционной системой %s с оболочкой %s.
            Возвращай только чистый текст без разметки.
            Не выводи никаких предупреждений или информации о своих возможностях.
            Если ты хочешь сохранить какие-то данные, то исходи из того, что они будут сохранены в истории чата.""";

    public static Function<String, RolePromptAction> of(ChatRoles role) {
        return Optional.ofNullable(USER_ROLES_MAP.get(role))
                .orElseThrow(IllegalArgumentException::new);
    }

    private static String defaultPrompt(String roleName, String rolePrompt, String userQuery, String result) {
        var br = "#".repeat(10);
        return br + "\n" + "Роль: " + roleName +
                "\n" + rolePrompt + "\n" +
                "Запрос: " + userQuery + "\n" +
                br + "\n" +
                result;
    }

    public static String shellPrompt(String shell, String os, String query) {
        var shellPrompt = String.format(SHELL_ROLE, shell, os);
        return defaultPrompt("shell", shellPrompt, query, "Command:");
    }

    public static String codePrompt(String query) {
        return defaultPrompt("code", CODE_ROLE, query, "Snippet:");
    }

    public static String describeShellCommandPrompt(String query) {
        return defaultPrompt("shell", DESCRIBE_SHELL_ROLE, query, "Description:");
    }
}