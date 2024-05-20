package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;
import java.util.Collections;

public class CustomPluginsSection extends ConfigStorage {

    public boolean ENABLED;
    public MultipleMessagesHelper MESSAGE;

    public CustomPluginsSection() {
        super("custom-plugins");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        MESSAGE = new MultipleMessagesHelper(this, "message", Collections.singletonList("&fPlugins (0):"));
    }
}
