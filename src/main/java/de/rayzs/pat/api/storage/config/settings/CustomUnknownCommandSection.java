package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.configuration.helper.*;
import de.rayzs.pat.api.storage.Storage;
import java.util.Collections;

public class CustomUnknownCommandSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper MESSAGE;

    public CustomUnknownCommandSection() {
        super("custom-unknown-command");
    }

    @Override
    public void load() {
        super.load();

        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) return;


        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            Logger.warning("It's not recommended to use the 'custom-unknown-command' feature when you are already using ProAntiTab in WHITELIST mode! Since in this mode, all commands except the once you specifically allow, are blocked anyway. So instead, change the 'blocked' message to your desired message. It has the same effect.");
        }


        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&cThis command does not exist!"));
    }
}
