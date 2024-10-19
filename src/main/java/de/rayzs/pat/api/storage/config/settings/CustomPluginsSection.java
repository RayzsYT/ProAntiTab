package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;
import java.util.Collections;

public class CustomPluginsSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper MESSAGE, COMMANDS;

    public CustomPluginsSection() {
        super("custom-plugins");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        COMMANDS = new MultipleMessagesHelper(this, "commands", Arrays.asList("pl", "plugins", "bukkit:pl", "bukkit:plugins"));
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&fPlugins (0):"));
    }

    public boolean isCommand(String command) {
        if (!ENABLED) return false;

        if (command.contains(" ")) {
            String[] split = command.split(" ");
            if (split.length > 0)
                command = split[0];
            command = command.split(" ")[0];
        }
        for (String currentCommand : COMMANDS.getLines()) {
            if (currentCommand.equals(command.toLowerCase()))
                return true;
        }
        return false;
    }
}
