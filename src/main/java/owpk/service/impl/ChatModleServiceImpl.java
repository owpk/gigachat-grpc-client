package owpk.service.impl;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import owpk.grpc.GigaChatGRpcClient;
import owpk.service.ChatModelService;

@Slf4j
public class ChatModleServiceImpl implements ChatModelService {
    private final GigaChatGRpcClient gRpcClient;

    public ChatModleServiceImpl(GigaChatGRpcClient gRpcClient) {
        this.gRpcClient = gRpcClient;
    }

    @Override
    public List<String> getModels() {
        return gRpcClient.modelList().getModelsList().stream()
            .map(rpcModel -> rpcModel.getName())
            .toList();
    }
    
}
