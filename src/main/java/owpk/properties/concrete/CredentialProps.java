package owpk.properties.concrete;

import java.util.List;
import java.util.Locale;

import owpk.model.JwtRestResponse;
import owpk.properties.PropertiesProvider;
import owpk.properties.PropertyDef;
import owpk.properties.PropertyValidationException;
import owpk.storage.RootedStorage;
import owpk.storage.Storage;
import owpk.storage.impl.LocalFileStorage;

public class CredentialProps extends PropertiesProvider {
    public static final PropertyDef DEF_COMPOSED_CREDENTIALS = new PropertyDef("composed_credentials", "");
    public static final PropertyDef DEF_JWT_ACCESS_TOKEN = new PropertyDef("jwt_accessToken", "");
    public static final PropertyDef DEF_JWT_EXPIRES_AT = new PropertyDef("jwt_expiresAt", "");

    public CredentialProps(Storage storage) {
        super("creds.properties", storage);
    }

    public CredentialProps() {
        this(new RootedStorage(MainProps.DEF_GIGACHAT_CLI_HOME.value(), new LocalFileStorage()));
    }

    @Override
    public void bootstrapValidation() {
        if (checkIfNull(DEF_COMPOSED_CREDENTIALS)) {
            throw new PropertyValidationException(DEF_COMPOSED_CREDENTIALS, String.format("""
            is empty.
                You need to write your basic auth credentials in '%s' property (or use 'gigachat config -d <credentials>')
                Please visit https://developers.sber.ru/docs/%s/gigachat/api/reference/rest/post-token for more information.
            """, DEF_COMPOSED_CREDENTIALS.key(), Locale.getDefault().getLanguage().toLowerCase()));
        }
    }

    public JwtRestResponse getJwt() {
        return new JwtRestResponse(
            getProperty(CredentialProps.DEF_JWT_ACCESS_TOKEN),
            Long.valueOf(getProperty(CredentialProps.DEF_JWT_EXPIRES_AT))
        );
    }


    @Override
    protected List<PropertyDef> getDefaultProperties() {
        return List.of(
            DEF_COMPOSED_CREDENTIALS,
            DEF_JWT_ACCESS_TOKEN,
            DEF_JWT_EXPIRES_AT
            );
    }

    public String getComposedCredentials() {
        return getProperty(DEF_COMPOSED_CREDENTIALS);
    }

    public synchronized void rewriteJwt(JwtRestResponse jwt) {
        setProperty(DEF_JWT_ACCESS_TOKEN, jwt.getAccessToken());
        setProperty(DEF_JWT_EXPIRES_AT, String.valueOf(jwt.getExpiresAt()));
    }
}
