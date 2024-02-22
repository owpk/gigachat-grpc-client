package owpk.service;

import owpk.model.PromptRole;

public interface ChatService {
    void chat(PromptRole promptRole, int lastMessageCount);
    void chat(PromptRole promptRole);

    void setUnaryMode();
    void setStreamMode();
}
