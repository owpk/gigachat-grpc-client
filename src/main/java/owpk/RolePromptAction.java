package owpk;

public interface RolePromptAction {
    default String getMessageRole() {
        return GigaChatConstants.MessageRole.SYSTEM;
    }

    String getRolePrompt();
}