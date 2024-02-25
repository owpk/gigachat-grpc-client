package owpk.service;

import owpk.role.RolePrompt;

public interface ChatService {

    void chat(RolePrompt rolePrompt);

    void setUnaryMode();

    void setStreamMode();
}
