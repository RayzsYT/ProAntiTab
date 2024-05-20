package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.*;
import java.util.Collections;

public class CancelCommandSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper MESSAGE;

    public CancelCommandSection() {
        super("cancel-blocked-commands");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&cThe command %command% is blocked!"));
    }
}
