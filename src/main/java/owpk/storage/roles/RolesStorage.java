package owpk.storage.roles;

import org.yaml.snakeyaml.Yaml;
import owpk.role.CodeRolePrompt;
import owpk.role.DescribeRolePrompt;
import owpk.role.ShellRolePrompt;
import owpk.storage.AbsPropertiesFileStorage;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static owpk.storage.roles.RoleField.*;

public class RolesStorage extends AbsPropertiesFileStorage<Map<String, Role>> {
    private static final String LIST_KEY = "roles";
    public static final String FILE_NAME = "roles.yml";

    public Role getRole(String name) {
        return Optional.ofNullable(this.settings.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + name));
    }

    @Override
    protected Map<String, Role> initSettings() {
        var data = loadFromYml();
        var mainList = data.get(LIST_KEY);
        return mainList.stream().collect(
                Collectors.toMap(it -> it.get(ROLE_NAME.getKey()), this::toRole));
    }

    private Map<String, List<Map<String, String>>> loadFromYml() {
        try (var inputStream = new FileInputStream(settingsFile.toFile())) {
            var yml = new Yaml();
            return yml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void load() {
        var mainList = loadFromYml();
        var converted = toProps(mainList);
        properties = new Properties();
        properties.putAll(converted);
    }

    @Override
    protected Path initSettingsFile() {
        return defaultInit(FILE_NAME);
    }

    @Override
    public Properties storeSettings(Map<String, Role> settings) {
        return null;
    }

    /**
     * Storing default properties to its settings file
     */
    @Override
    public void createDefaults() {
        var yml = new Yaml();
        try(var fos = new FileWriter(settingsFile.toFile())) {
            var dump = toYml(initDefaultRoles());
            yml.dump(dump, fos);
            properties = toProps(dump);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Role> initDefaultRoles() {
        return List.of(
                new Role(ShellRolePrompt.NAME, ShellRolePrompt.SHELL_ROLE, "", "Shell command"),
                new Role(CodeRolePrompt.NAME, CodeRolePrompt.CODE_ROLE, "", "Code snippet"),
                new Role(DescribeRolePrompt.NAME, DescribeRolePrompt.DESCRIBE_SHELL_ROLE, "", "Command description")
                );
    }

    private Map<String, Map<String, String>> basicMap(List<Map<String, String>> roles) {
        return roles.stream().collect(
                Collectors.toMap(it -> it.get("name"),
                        Function.identity()));
    }

    private Map<String, String> toMap(Role role) {
        return Map.of(ROLE_NAME.getKey(), role.roleName(),
                ROLE_PROMPT.getKey(), role.prompt(),
                ROLE_DESCRIPTION.getKey(), role.description(),
                ROLE_EXPECTED.getKey(), role.expected());
    }

    private Role toRole(Map<String, String> map) {
        return new Role(map.get(ROLE_NAME.getKey()),
                map.get(ROLE_PROMPT.getKey()),
                map.get(ROLE_DESCRIPTION.getKey()),
                map.get(ROLE_EXPECTED.getKey()));
    }

    private Map<String, List<Map<String, String>>> toYml(List<Role> roles) {
        Map<String, List<Map<String, String>>> data = new HashMap<>();
        var mainList = new ArrayList<Map<String, String>>();
        data.put(LIST_KEY, mainList);
        roles.forEach(it -> mainList.add(toMap(it)));
        return data;
    }

    private Properties toProps(Map<String, List<Map<String, String>>> dump) {
        Properties properties = new Properties();
        var collected = dump.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(it -> it.get(ROLE_NAME.getKey()), Function.identity()))
                .entrySet().stream().flatMap(it -> it.getValue().entrySet().stream()
                        .collect(Collectors.toMap(e -> LIST_KEY + "." + it.getKey() + "." + e, e -> it.getKey()))
                        .entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        properties.putAll(collected);
        return properties;
    }
}
