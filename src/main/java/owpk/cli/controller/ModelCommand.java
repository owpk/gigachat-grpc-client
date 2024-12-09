package owpk.cli.controller;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import owpk.output.OutputEngine;
import owpk.service.ChatModelService;
import owpk.utils.LoggingUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "model", aliases = {"m"}, description = "Retrieve available GigaChat models")
@Slf4j
public class ModelCommand implements Runnable {
    private final ChatModelService modelService;
    private final OutputEngine outputEngine;

    @Inject
    public ModelCommand(OutputEngine outputEngine, ChatModelService modelService) {
        this.outputEngine = outputEngine;
        this.modelService = modelService;
    }

    @Override
    public void run() {
        LoggingUtils.cliCommandLog(this.getClass(), log);
        var model = modelService.getModels();
        model.forEach(m -> outputEngine.out(m + "\n"));
    }
}
