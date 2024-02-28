package owpk.cli;

import picocli.CommandLine;

import java.util.Properties;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() throws Exception {
        var url = getClass().getResource("/version");
        var props = new Properties();
        assert url != null;
        props.load(url.openStream());
        return new String[]{props.getProperty("version")};
    }
}
