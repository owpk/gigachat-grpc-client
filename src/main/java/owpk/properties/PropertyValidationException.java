package owpk.properties;

public class PropertyValidationException extends RuntimeException {
    public PropertyValidationException(PropertyDef property, String message) {
        super("Property '" + property.key() + "': " + message);
    }
}
