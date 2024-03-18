package owpk.storage;

import java.util.List;

// TODO main storage handler
public class StorageManager {
    List<FileSettingsStore<?>> storages;

    public void forceCreateDefaults() {
        storages.forEach(FileSettingsStore::createDefaults);
    }

}
