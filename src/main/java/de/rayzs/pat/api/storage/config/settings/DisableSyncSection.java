package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class DisableSyncSection extends ConfigStorage {

    public boolean DISABLED;

    public DisableSyncSection() {
        super("disable-sync");
    }

    @Override
    public void load() {
        super.load();

        if(!Reflection.isProxyServer()) return;
        DISABLED = new ConfigSectionHelper<Boolean>(this, null, false).getOrSet();
    }
}
