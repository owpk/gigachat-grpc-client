package owpk.role;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import owpk.GigaChatConstants;

@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class SystemRolePrompt extends RolePrompt {

    public SystemRolePrompt(String userQuery, String chatRoleName) {
        this(userQuery, chatRoleName, 0);
    }

    public SystemRolePrompt(String userQuery, String chatRoleName, int chatHistorySize) {
        super(userQuery, chatRoleName, GigaChatConstants.MessageRole.SYSTEM.getValue(), chatHistorySize);
    }

    protected static String defaultPrompt(String chatRoleName, String userQuery, String chatRolePrompt, String expecting) {
        var br = "#".repeat(8);
        return br + "\n" + "Роль: " + chatRoleName+
                "\n" + chatRolePrompt + "\n" +
                "Запрос: " + userQuery + "\n" +
                br + "\n" +
                expecting + ":";
    }

}