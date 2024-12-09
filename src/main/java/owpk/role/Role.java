package owpk.role;

public record Role(
        String roleName,
        String prompt,
        String description,
        String expected) {
}
