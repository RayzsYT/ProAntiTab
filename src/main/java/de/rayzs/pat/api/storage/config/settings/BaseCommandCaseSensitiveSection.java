package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Collections;

public class BaseCommandCaseSensitiveSection extends ConfigStorage {

    public boolean ENABLED;

    public BaseCommandCaseSensitiveSection() {
        super("base-command-case-sensitive");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, null, true).getOrSet();
    }
}
