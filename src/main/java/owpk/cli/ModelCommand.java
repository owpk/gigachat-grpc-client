package owpk.cli;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.grpc.GigaChatGRpcClient;
import owpk.properties.concrete.MainProps;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "model", aliases = {"m"}, description = "Retrieve available GigaChat models")
@Slf4j
public class ModelCommand implements Runnable {
    private final GigaChatGRpcClient gigaChatGRpcClient;
    private final MainProps mainProps;

    @Inject
    public ModelCommand(GigaChatGRpcClient gigaChatGRpcClient, MainProps mainProps) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
        this.mainProps = mainProps;
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        var response = gigaChatGRpcClient.modelList();
        response.getModelsList().forEach(it ->
                System.out.println(it.getName()));
    }
}
