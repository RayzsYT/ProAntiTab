package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;
import java.util.Collections;

public class CustomVersionSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper MESSAGE, COMMANDS;

    public CustomVersionSection() {
        super("custom-version");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        COMMANDS = new MultipleMessagesHelper(this, "commands", Arrays.asList("icanhasbukkit", "ver", "version", "bukkit:ver", "bukkit:version"));
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&fThis server is running CraftBukkit version git-NasaSpigot-294 (MC: X)"));
    }

    public boolean isCommand(String command) {
        if (!ENABLED) return false;
        command = StringUtils.getFirstArg(command);

        for (String currentCommand : COMMANDS.getLines()) {
            if (currentCommand.equals(command.toLowerCase()))
                return true;
        }
        return false;
    }
}
