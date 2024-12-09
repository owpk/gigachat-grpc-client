package owpk.properties.concrete;

import java.util.List;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import owpk.properties.PropertiesProvider;
import owpk.properties.PropertyDef;
import owpk.storage.RootedStorage;
import owpk.storage.Storage;
import owpk.storage.impl.LocalFileStorage;

@Slf4j
@Getter
public class MainProps extends PropertiesProvider {
    public static final PropertyDef DEF_GIGACHAT_CLI_HOME = new PropertyDef("gigachat_cli_home", System.getProperty("user.home") + "/.gigachat-cli/");
    public static final PropertyDef DEF_AUTH_URI = new PropertyDef("gigachat_auth_uri", "https://ngw.devices.sberbank.ru:9443/api/v2/oauth");
    public static final PropertyDef DEF_TARGET = new PropertyDef("gigachat_grpc_target", "gigachat.devices.sberbank.ru");

    public static final PropertyDef DEF_CHATS_HISTORY_MODE = new PropertyDef("chat_history_mode", "local");
    public static final PropertyDef DEF_CHATS_HISTORY_HOME = new PropertyDef("chat_history_home", "/chats/");
    public static final PropertyDef DEF_CURRENT_CHAT_NAME = new PropertyDef("chat_current_chat", "");

    public static final PropertyDef DEF_ROLES_YML = new PropertyDef("roles_yml", "roles.yml");
    public static final PropertyDef DEF_CURRENT_MODEL = new PropertyDef("current_model", "GigaChat");
    public static final PropertyDef DEF_FALLBACK_MODEL = new PropertyDef("fallback_model", "GigaChat");

    public MainProps(String propName, Storage storage) {
        super(propName, storage);
    }

    public MainProps() {
        super("main.properties",
            new RootedStorage(DEF_GIGACHAT_CLI_HOME.value(), new LocalFileStorage()));
    }

    @Override
    public void bootstrapValidation() {
        getDefaultProperties()
            .forEach(it -> this.acceptEmpty(it, empty -> {
                log.warn(propertyName + ": " + empty.key() + " is empty.");
                setProperty(empty, findDefaultValueByKey(it));
            }));
    }

    @Override
    protected List<PropertyDef> getDefaultProperties() {
        return List.of(
            DEF_CHATS_HISTORY_MODE,
            DEF_CHATS_HISTORY_HOME,
            DEF_CURRENT_CHAT_NAME,

            DEF_ROLES_YML,

            DEF_CURRENT_MODEL,
            DEF_FALLBACK_MODEL,
            DEF_GIGACHAT_CLI_HOME,
            DEF_AUTH_URI,
            DEF_TARGET);
    }
}