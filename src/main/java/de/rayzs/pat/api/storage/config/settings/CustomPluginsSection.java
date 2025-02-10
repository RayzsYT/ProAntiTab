package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;
import java.util.*;

public class CustomPluginsSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_TAB_COMPLETABLE;
    public MultipleMessagesHelper MESSAGE, COMMANDS;

    public CustomPluginsSection() {
        super("custom-plugins");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        ALWAYS_TAB_COMPLETABLE = new ConfigSectionHelper<Boolean>(this, "always-tab-completable", false).getOrSet();
        COMMANDS = new MultipleMessagesHelper(this, "commands", Arrays.asList("pl", "plugins", "bukkit:pl", "bukkit:plugins"));
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&fPlugins (0):"));
    }

    public boolean isTabCompletable(String command) {
        if (!isCommand(command))
            return false;

        return ALWAYS_TAB_COMPLETABLE;
    }

    public boolean isCommand(String command) {
        if(!ENABLED) return false;

        if (command.contains(" ")) {
            String[] split = command.split(" ");
            if (split.length > 0)
                command = split[0];
            command = command.split(" ")[0];
        }
        for (String currentCommand : COMMANDS.getLines()) {
            if (currentCommand.equalsIgnoreCase(command))
                return true;
        }
        return false;
    }
}
