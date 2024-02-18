package owpk.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "gigachat-cli", description = "GigaChat CLI. Use -h or --help for more information",
        mixinStandardHelpOptions = true, subcommands = {ChatRequestCommand.class, ConfigCommand.class, ModelCommand.class})
public class GigachatCommand implements Runnable {
    // TODO add help
    @CommandLine.Option(names = {"-h", "--help"}, description = "Display help information.")
    boolean showHelp;

    @Override
    public void run() {
        if (showHelp) {
            System.out.println("""
                    Please visit
                    \thttps://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    \tfor more information
                    """);
            System.out.println("""
                    Use config -s to show config file properties
                    \tYou need to write your credentials in 'gigachat.composedCredentials' property
                    \tfind how to retrieve credentials: https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-token
                    """);
            CommandLine.usage(this, System.out);
        }
    }
}
