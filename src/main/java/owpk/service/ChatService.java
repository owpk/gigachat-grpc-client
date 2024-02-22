package owpk.service;

public interface ChatService {
    void chat(String query, int lastMessageCount);
    void shell(String query);
    void code(String query);
}
