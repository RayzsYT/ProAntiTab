package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.StringUtils;

public class BlockNamespaceCommandsSection extends ConfigStorage {

    public boolean ENABLED;

    public BlockNamespaceCommandsSection() {
        super("block-namespace-commands");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
    }

    public boolean isCommand(String command) {
        if(!ENABLED) return false;
        return StringUtils.getFirstArg(command).contains(":");
    }
}
