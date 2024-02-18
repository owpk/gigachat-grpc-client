package owpk.cli;

import jakarta.inject.Inject;
import owpk.grpc.GigaChatGRpcClient;
import picocli.CommandLine;

@CommandLine.Command(name = "model", description = "Search questions matching criteria.")
public class ModelCommand implements Runnable {
    private final GigaChatGRpcClient gigaChatGRpcClient;

    @Inject
    public ModelCommand(GigaChatGRpcClient gigaChatGRpcClient) {
        this.gigaChatGRpcClient = gigaChatGRpcClient;
    }

    @Override
    public void run() {
        var response = gigaChatGRpcClient.modelList();
        response.getModelsList().forEach(it ->
                System.out.println(it.getName()));
    }
}
