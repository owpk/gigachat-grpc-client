package owpk.service;

import owpk.role.RolePrompt;

public interface ChatService {

    void chat(RolePrompt rolePrompt);

    void setChatRequestHandler(ChatRequestHandler chatRequestHandler);

    void setUnaryMode();

    void setStreamMode();
}
