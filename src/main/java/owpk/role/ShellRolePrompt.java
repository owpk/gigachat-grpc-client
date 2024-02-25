package owpk.role;

public class ShellRolePrompt extends SystemRolePrompt {
    private static final String SHELL_ROLE = """
            Возвращай только команды оболочки %s для операционной системы %s без пояснений.
            Если недостаточно деталей, то предоставь наиболее логичное решение.
            Убедись, что ты возвращаешь корректную shell команду.
            Если требуется несколько команд, постарайся объединить их в одну.""";

    public ShellRolePrompt(String userQuery) {
        super(userQuery, "shell");
    }

    @Override
    public String getRolePrompt() {
        return SystemRolePrompt.defaultPrompt(chatRoleName, userQuery, SHELL_ROLE, "Command:");
    }
}