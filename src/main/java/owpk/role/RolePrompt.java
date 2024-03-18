package owpk.role;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class RolePrompt implements RolePromptAction {
    String userQuery;
    String chatRoleName;
    String messageRoleName;
    int chatHistoryContextSize;
}