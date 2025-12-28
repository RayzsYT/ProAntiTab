package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.sender.CommandSender;

public class BlockNamespaceCommandsSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_BLOCK_COMMAND;

    public BlockNamespaceCommandsSection() {
        super("block-namespace-commands");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        ALWAYS_BLOCK_COMMAND = new ConfigSectionHelper<Boolean>(this, "always-block-command", true).getOrSet();
    }

    public boolean doesAlwaysBlock(String command) {
        return ALWAYS_BLOCK_COMMAND && isCommand(command);
    }

    public boolean isCommand(String command) {
        if (!ENABLED) {
            return false;
        }

        return StringUtils.getFirstArg(command).contains(":");
    }

    public boolean doesBypass(CommandSender sender) {
        return !ENABLED || sender.hasPermission("namespace");
    }
}
