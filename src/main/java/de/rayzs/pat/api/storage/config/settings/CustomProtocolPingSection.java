package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class CustomProtocolPingSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_SHOW, HIDE_PLAYERS;
    public String PROTOCOL;

    public CustomProtocolPingSection() {
        super("custom-protocol-ping");
    }

    @Override
    public void load() {
        super.load();
        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", Reflection.isProxyServer() || Reflection.isPaper()).getOrSet();
        ALWAYS_SHOW = new ConfigSectionHelper<Boolean>(this, "always-show", Reflection.isProxyServer() || Reflection.isPaper()).getOrSet();
        PROTOCOL = new ConfigSectionHelper<String>(this, "protocol", "&f&lProAntiTab &7(&a%online%&7/&c%max%&7)").getOrSet();
        if(!Reflection.isProxyServer()) return;
        HIDE_PLAYERS = new ConfigSectionHelper<Boolean>(this, "hide-players", true).getOrSet();
    }
}
