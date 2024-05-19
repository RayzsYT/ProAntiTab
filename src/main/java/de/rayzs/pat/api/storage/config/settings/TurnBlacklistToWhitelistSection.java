package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

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
