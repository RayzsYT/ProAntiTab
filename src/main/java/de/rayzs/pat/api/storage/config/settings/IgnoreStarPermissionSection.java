package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class IgnoreStarPermissionSection extends ConfigStorage {

    public boolean ENABLED;

    public IgnoreStarPermissionSection() {
        super("ignore-star-permission");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, null, false).getOrSet();
    }
}
