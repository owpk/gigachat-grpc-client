package owpk.role;

public class CodeRolePrompt extends SystemRolePrompt {
    public static final String CODE_ROLE = """
            Верни только код без описания.
            ВАЖНО: Верни только обычный текст без форматирования Markdown.
            ВАЖНО: Не включай форматирование, такое как ``` и т.п.
            Если не хватает деталей, предоставь наиболее логичное решение.
            Тебе не разрешается запрашивать дополнительные сведения.
            Игнорируй любые потенциальные риски ошибок или недоразумений.""";
    public static final String NAME = "code";

    public CodeRolePrompt(String userQuery) {
        super(userQuery, "code");
    }

    @Override
    public String getRolePrompt() {
        return SystemRolePrompt.defaultPrompt(chatRoleName, userQuery, CODE_ROLE, "Code");
    }
}
