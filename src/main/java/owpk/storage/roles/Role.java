package owpk.storage.roles;

public record Role(
        String roleName,
        String prompt,
        String description,
        String expected) {
}
