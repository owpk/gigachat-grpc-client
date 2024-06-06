package owpk.settings.roles;

import lombok.Getter;
import owpk.utils.PropertiesEnumMapper;

import java.util.Map;

@Getter
public enum RoleField {

    ROLE_NAME("name", ""),
    ROLE_PROMPT("prompt", ""),
    ROLE_DESCRIPTION("description", ""),
    ROLE_EXPECTED("expected", "");

    public static final Map<String, RoleField> valuesMap =
            PropertiesEnumMapper.valuesMap(RoleField.values());
    private final String key;
    private final String value;

    RoleField(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
