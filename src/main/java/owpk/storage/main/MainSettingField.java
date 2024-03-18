package owpk.storage.main;

import lombok.Getter;
import owpk.utils.PropertiesEnumMapper;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum MainSettingField {
    COMPOSED_CREDENTIALS("gigachat.composedCredentials", ""),
    MODEL("gigachat.model", "GigaChat:latest"),
    TARGET("gigachat.target", "gigachat.devices.sberbank.ru"),
    JWT_ACCESS_TOKEN("gigachat.jwt.accessToken", "NOT_VALID_JWT"),
    JWT_EXPIRES_AT("gigachat.jwt.expiresAt", "0"),
    AUTH_URI("gigachat.authUri", "https://ngw.devices.sberbank.ru:9443/api/v2/oauth");

    public static final Map<String, MainSettingField> valuesMap =
            PropertiesEnumMapper.valuesMap(MainSettingField.values());

    public static final Map<String, String> propertiesMap =
            valuesMap.values().stream()
            .collect(Collectors.toMap(MainSettingField::getPropertyKey, MainSettingField::getValue));

    private final String propertyKey;
    private final String value;

    MainSettingField(String propertyKey, String value) {
        this.propertyKey = propertyKey;
        this.value = value;
    }

    public static MainSettingField getByName(String name) {
        return Optional.ofNullable(valuesMap.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown setting field: " + name));
    }

    public static String getValueByName(String name) {
        return Optional.ofNullable(propertiesMap.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown property: " + name));
    }

}