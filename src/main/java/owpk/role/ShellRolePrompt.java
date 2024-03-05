package owpk.role;

public class ShellRolePrompt extends SystemRolePrompt {
    public static final String SHELL_ROLE = """
            Возвращай только команды оболочки bash для операционной системы {os}.
            Предоставь только обычный текст без форматирования Markdown и только команду без пояснений.
            Если недостаточно деталей, то предоставь наиболее логичное решение.
            Убедись, что ты возвращаешь корректную shell команду.
            Если требуется несколько команд, постарайся объединить их в одну.""";
    public static final String NAME = "shell";

    public ShellRolePrompt(String userQuery) {
        super(userQuery, "shell");
    }

    @Override
    public String getRolePrompt() {
        return SystemRolePrompt.defaultPrompt(chatRoleName, userQuery, SHELL_ROLE, "Command:");
    }
}