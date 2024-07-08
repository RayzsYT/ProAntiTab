package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;

public class TurnBlacklistToWhitelistSection extends ConfigStorage {

    public boolean ENABLED;

    public TurnBlacklistToWhitelistSection() {
        super("turn-blacklist-to-whitelist");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, null, false).getOrSet();
    }
}
