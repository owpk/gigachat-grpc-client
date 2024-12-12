package owpk.service;

import gigachat.v1.Gigachatv1;

@FunctionalInterface
public interface ChatRequestHandler {
    String handleChatRequest(Gigachatv1.ChatRequest request);
}
