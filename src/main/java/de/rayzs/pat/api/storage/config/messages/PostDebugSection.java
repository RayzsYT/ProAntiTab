package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;

public class PostDebugSection extends ConfigStorage {

    public String SUCCESS, FAILED;

    public PostDebugSection() {
        super("post-debug");
    }

    @Override
    public void load() {
        super.load();
        SUCCESS = new ConfigSectionHelper<String>(this, "success", "&aSuccessfully uploaded debug logs: &e%link% &8(<click:open_url:%link%><gray>CLICK</click>&8)").getOrSet();
        FAILED = new ConfigSectionHelper<String>(this, "failed", "&cFailed to upload debug logs!").getOrSet();
    }
}
