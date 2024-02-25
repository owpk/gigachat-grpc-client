package owpk.role;

public class DescribeRolePrompt extends SystemRolePrompt {
    private static final String DESCRIBE_SHELL_ROLE = """
            Предоставь краткое описание, одним предложением, данной команды.
            Предоставь только обычный текст без форматирования Markdown.
            Не показывай никаких предупреждений или информации о своих возможностях.
            Если тебе нужно хранить какие-либо данные, предположите, что они будут храниться в чате.
            """;

    public DescribeRolePrompt(String userQuery) {
        super(userQuery, "describe shell");
    }

    @Override
    public String getRolePrompt() {
        return SystemRolePrompt.defaultPrompt(chatRoleName, userQuery, DESCRIBE_SHELL_ROLE, "Description:");
    }
}
