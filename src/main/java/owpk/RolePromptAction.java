package owpk;

public interface RolePromptAction {
    default String getMessageRole() {
        return GigaChatConstants.Role.SYSTEM;
    }

    String getRolePrompt();
}