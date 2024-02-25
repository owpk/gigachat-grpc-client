package owpk.role;

import owpk.GigaChatConstants;

public class DefaultChatRolePrompt extends RolePrompt {

    public DefaultChatRolePrompt(String userQuery, int chatHistoryContextSize) {
        super(userQuery, "chat", GigaChatConstants.MessageRole.USER.getValue(), chatHistoryContextSize);
    }

    @Override
    public String getRolePrompt() {
        return userQuery;
    }
}
