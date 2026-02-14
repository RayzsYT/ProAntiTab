package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class AutoLowercaseCommandsSection extends ConfigStorage {

    public boolean ENABLED;

    public AutoLowercaseCommandsSection() {
        super("auto-lowercase-commands");
    }

    @Override
    public void load() {
        super.load();

        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
    }

    public boolean isCommand(String command) {
        if(!ENABLED) return false;

        return !StringUtils.isLowercased(command);
    }
}
