package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class CustomProtocolPingSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_SHOW, HIDE_PLAYERS, USE_EXTEND_AS_MAX_COUNT;
    public String PROTOCOL;
    public int EXTEND_COUNT;

    public CustomProtocolPingSection() {
        super("custom-protocol-ping");
    }

    @Override
    public void load() {
        super.load();
        if (!Reflection.isProxyServer()) {
            if (!Reflection.isPaper() || Reflection.getMinor() < 12)
                return;
        }

        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        ALWAYS_SHOW = new ConfigSectionHelper<Boolean>(this, "always-show", Reflection.isProxyServer() || Reflection.isPaper()).getOrSet();
        USE_EXTEND_AS_MAX_COUNT = new ConfigSectionHelper<Boolean>(this, "use-as-maxplayers", false).getOrSet();
        PROTOCOL = new ConfigSectionHelper<String>(this, "protocol", "&f&lProAntiTab &7(&a%online%&7/&c%max%&7)").getOrSet();
        EXTEND_COUNT = new ConfigSectionHelper<Integer>(this, "extend-online-count", 1).getOrSet();
        if (Reflection.isProxyServer()) return;
        HIDE_PLAYERS = new ConfigSectionHelper<Boolean>(this, "hide-players", true).getOrSet();
    }
}
