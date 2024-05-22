package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class PrefixSection extends ConfigStorage {

    public String PREFIX;

    public PrefixSection() {
        super("prefix");
    }

    @Override
    public void load() {
        super.load();
        PREFIX = new ConfigSectionHelper<String>(this, null, "&8[&fPAT&8]").getOrSet();
    }
}
