package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class UpdateGroupsPerServerSection extends ConfigStorage {

    public boolean ENABLED;

    public UpdateGroupsPerServerSection() {
        super("update-groups-per-server");
    }

    @Override
    public void load() {
        super.load();

        if (!Reflection.isProxyServer())
            return;

        ENABLED = new ConfigSectionHelper<Boolean>(this, null, false).getOrSet();
    }
}
