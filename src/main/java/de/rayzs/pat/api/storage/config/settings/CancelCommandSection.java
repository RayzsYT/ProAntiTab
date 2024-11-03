package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;
import java.util.Collections;

public class CancelCommandSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper BASE_COMMAND_RESPONSE, SUB_COMMAND_RESPONSE;

    public CancelCommandSection() {
        super("cancel-blocked-commands");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        BASE_COMMAND_RESPONSE = new MultipleMessagesHelper(this, "base-command-message", Collections.singletonList("&cThe command %command% is blocked!"));
        SUB_COMMAND_RESPONSE = new MultipleMessagesHelper(this, "sub-command-message", Collections.singletonList("&cThis sub-argument is blocked!"));
    }
}
