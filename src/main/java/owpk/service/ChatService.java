package owpk.service;

import owpk.RolePromptAction;
import owpk.model.PromptRole;

public interface ChatService {
    void chat(RolePromptAction promptRole, int lastMessageCount);
    void chat(RolePromptAction promptRole);

    void setUnaryMode();
    void setStreamMode();
}
