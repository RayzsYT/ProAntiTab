package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;
import de.rayzs.pat.utils.StringUtils;
import java.util.*;

public class CustomVersionSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_TAB_COMPLETABLE;
    public MultipleMessagesHelper MESSAGE, COMMANDS;

    public CustomVersionSection() {
        super("custom-version");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        ALWAYS_TAB_COMPLETABLE = new ConfigSectionHelper<Boolean>(this, "always-tab-completable", false).getOrSet();
        COMMANDS = new MultipleMessagesHelper(this, "commands", Arrays.asList("icanhasbukkit", "about", "ver", "version"));
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&fThis server is running CraftBukkit version git-NasaSpigot-294 (MC: X)"));
    }

    public boolean isTabCompletable(String command) {
        if (!isCommand(command))
            return false;

        return ALWAYS_TAB_COMPLETABLE;
    }

    public boolean isCommand(String command) {
        if(!ENABLED) return false;
        command = StringUtils.getFirstArg(command);

        for (String currentCommand : COMMANDS.getLines()) {
            if (currentCommand.equalsIgnoreCase(command))
                return true;
        }
        return false;
    }
}
