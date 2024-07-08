package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;

public class ReloadSection extends ConfigStorage {

    public String LOADING, DONE;

    public ReloadSection() {
        super("reload");
    }

    @Override
    public void load() {
        super.load();
        LOADING = new ConfigSectionHelper<String>(this, "loading", "&eReloading all configuration files...").getOrSet();
        DONE = new ConfigSectionHelper<String>(this, "done", "&aSuccessfully reloaded all configuration files!").getOrSet();
    }
}
