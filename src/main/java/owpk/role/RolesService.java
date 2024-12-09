package owpk.role;

import static owpk.role.RoleField.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import owpk.properties.concrete.MainProps;
import owpk.storage.Storage;

public class RolesService {
    private static final String LIST_KEY = "roles";

    private final Storage storage;
    private final MainProps mainProps;
    private final Map<String, Role> roles = new HashMap<>();

    public RolesService(Storage storage, MainProps mainProps) {
        this.storage = storage;
        this.mainProps = mainProps;

        if (!storage.exists(mainProps.getProperty(MainProps.DEF_ROLES_YML))) {
            storage.createFileOrDirIfNotExists(mainProps.getProperty(MainProps.DEF_ROLES_YML));
            loadDefaultToYaml();
        }

        var yml = loadFromYml();
        var roles = yml.get(LIST_KEY);
        this.roles.putAll(roles.stream().map(this::toRole)
                .collect(Collectors.toMap(Role::roleName, Function.identity())));
    }

    public Role getRole(String name) {
        return roles.get(name);
    }

    private Map<String, List<Map<String, String>>> loadFromYml() {
        var data = storage.getContent(mainProps.getProperty(MainProps.DEF_ROLES_YML));
        var yml = new Yaml();
        return yml.load(new String(data));
    }

    private void loadDefaultToYaml() {
        var yml = new Yaml();
        var dumpedYml = yml.dump(toYml(initDefaultRoles()));
        storage.saveContent(mainProps.getProperty(MainProps.DEF_ROLES_YML), dumpedYml.getBytes(), false);
    }

    private List<Role> initDefaultRoles() {
        return List.of(
                    new Role(ShellRolePrompt.NAME, ShellRolePrompt.SHELL_ROLE, "", "Shell command"),
                    new Role(CodeRolePrompt.NAME, CodeRolePrompt.CODE_ROLE, "", "Code snippet"),
                    new Role(DescribeRolePrompt.NAME, DescribeRolePrompt.DESCRIBE_SHELL_ROLE, "", "Command description")
                );
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
}
