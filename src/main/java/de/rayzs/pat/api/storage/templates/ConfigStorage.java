package de.rayzs.pat.api.storage.templates;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.StorageTemplate;

public class ConfigStorage extends StorageTemplate {

    public ConfigStorage(String navigatePath) {
        super(Storage.Files.CONFIGURATION, navigatePath);
        Storage.ConfigSections.LIST.add(this);
    }

    @Override
    public void save() { getConfig().save(); }

    @Override
    public void load() { getConfig().reload(); }
}
