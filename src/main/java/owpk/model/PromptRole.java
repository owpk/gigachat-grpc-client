package owpk.model;

public record PromptRole(String userQuery,
                         String chatRoleName,
                         String rolePrompt,
                         String messageRoleName) {

}
