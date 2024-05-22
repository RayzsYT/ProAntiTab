package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class CustomProtocolPingSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_SHOW;
    public String PROTOCOL;

    public CustomProtocolPingSection() {
        super("custom-protocol-ping");
    }

    @Override
    public void load() {
        super.load();
        if(!Reflection.isProxyServer()) return;
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        ALWAYS_SHOW = new ConfigSectionHelper<Boolean>(this, "always-show", true).getOrSet();
        PROTOCOL = new ConfigSectionHelper<String>(this, "protocol", "&f&lProAntiTab &7(&a%online%&7/&c%max%&7)").getOrSet();
    }
}
