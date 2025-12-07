package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class AllowGroupOverrulingSection extends ConfigStorage {

    public boolean ENABLED;

    public AllowGroupOverrulingSection() {
        super("allow-group-overruling");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, null, false).getOrSet();
    }
}
