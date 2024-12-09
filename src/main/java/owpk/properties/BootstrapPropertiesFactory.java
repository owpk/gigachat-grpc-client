package owpk.properties;

import java.util.Map;
import java.util.WeakHashMap;

import owpk.properties.concrete.CredentialProps;
import owpk.properties.concrete.MainProps;
import owpk.storage.Storage;

public class BootstrapPropertiesFactory {
    private static final BootstrapPropertiesFactory INSTANCE = new BootstrapPropertiesFactory();

    private Map<Class<?>, PropertiesProvider> providers = new WeakHashMap<>();

    public static BootstrapPropertiesFactory getInstance() {
        return INSTANCE;
    }

    private BootstrapPropertiesFactory() {
        registerProviders();
    }

    private void registerProviders() {
        var main = createDefaultMainPropertiesProvider();
        createCredentialPropertiesProvider(main);
    }

    public static void validateProviders() throws PropertyValidationException {
        for (var provider : INSTANCE.providers.values()) {
            provider.bootstrapValidation();
        }
    }

    public PropertiesProvider getProvider(Class<?> providerClass) {
        return providers.get(providerClass);
    }

    private MainProps creatMainPropsProvider(String name, Storage storage) {
        return (MainProps) registerProvider(new MainProps(name, storage));
    }

    private MainProps createDefaultMainPropertiesProvider() {
        return (MainProps) registerProvider(new MainProps());
    }

    private CredentialProps createCredentialPropertiesProvider(PropertiesProvider mainProps) {
        return (CredentialProps) registerProvider(new CredentialProps(mainProps.getStorage()));
    }

    private PropertiesProvider registerProvider(PropertiesProvider provider) {
        this.providers.put(provider.getClass(), provider);
        return provider;
    }
    
}
