package de.rayzs.pat.api.storage.config.messages;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Collections;

public class NotificationSection extends ConfigStorage {

    public String ENABLED, DISABLED;
    public MultipleMessagesHelper ALERT;

    public NotificationSection() {
        super("notification");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<String>(this, "enabled", "&aEnabled notifications").getOrSet();
        DISABLED = new ConfigSectionHelper<String>(this, "disabled", "&cDisabled notifications").getOrSet();
        ALERT = new MultipleMessagesHelper(this, "alert", Collections.singletonList("&8[&4ALERT&8] &c%player% &8(&7" + (Reflection.isProxyServer() ? "server: &e%server%" : "world: &e%world%") + "&8) &ctried to execute the following blocked command: &4%command%"));
    }
}
