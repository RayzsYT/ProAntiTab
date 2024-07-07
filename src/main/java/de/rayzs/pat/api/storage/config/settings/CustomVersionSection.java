package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import java.util.Collections;

public class CustomVersionSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper MESSAGE;
    private final String[] PLUGIN_COMMANDS = new String[] { "icanhasbukkit", "ver", "version", "bukkit:ver", "bukkit:version" };

    public CustomVersionSection() {
        super("custom-version");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&fThis server is running CraftBukkit version git-ProSpigot-294 (MC: 1.8-1.21) (Implementing API version cool-api-version-SNAPSHOT)"));
    }

    public boolean isCommand(String command) {
        if(!ENABLED) return false;
        command = StringUtils.getFirstArg(command);

        for (String currentCommand : PLUGIN_COMMANDS) {
            if (currentCommand.equals(command.toLowerCase()))
                return true;
        }
        return false;
    }
}
