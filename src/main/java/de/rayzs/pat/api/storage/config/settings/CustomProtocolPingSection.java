package de.rayzs.pat.api.storage.config.settings;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.helper.MultipleMessagesHelper;

import java.util.Arrays;

public class CustomProtocolPingSection extends ConfigStorage {

    public boolean ENABLED, ALWAYS_SHOW, HIDE_PLAYERS, USE_EXTEND_AS_MAX_COUNT, USE_CUSTOM_PLAYERLIST;
    public String PROTOCOL;

    public MultipleMessagesHelper PLAYERLIST;

    public int EXTEND_COUNT;

    public CustomProtocolPingSection() {
        super("custom-protocol-ping");
    }

    @Override
    public void load() {
        super.load();

        if (!Reflection.isProxyServer() && (!Reflection.isPaper() || Reflection.getMinor() < 12))
            return;

        ENABLED = new ConfigSectionHelper<Boolean>(this, "enabled", true).getOrSet();
        ALWAYS_SHOW = new ConfigSectionHelper<Boolean>(this, "always-show", Reflection.isProxyServer() || Reflection.isPaper()).getOrSet();
        USE_EXTEND_AS_MAX_COUNT = new ConfigSectionHelper<Boolean>(this, "use-as-maxplayers", false).getOrSet();
        PROTOCOL = new ConfigSectionHelper<String>(this, "protocol", "&f&lProAntiTab &7(&a%online%&7/&c%max%&7)").getOrSet();
        EXTEND_COUNT = new ConfigSectionHelper<Integer>(this, "extend-online-count", 1).getOrSet();

        HIDE_PLAYERS = new ConfigSectionHelper<Boolean>(this, "hide-players", true).getOrSet();

        USE_CUSTOM_PLAYERLIST = new ConfigSectionHelper<Boolean>(this, "custom-playerlist.enabled", false).getOrSet();
        PLAYERLIST = new MultipleMessagesHelper(this, "custom-playerlist.list", Arrays.asList(
                "&8> &7Online players: &f%online%&7/&c%max%",
                "&8> &7Using &fProAntiTab"
        ));



        if (HIDE_PLAYERS && USE_CUSTOM_PLAYERLIST) {
            Logger.warning("You can't use both 'hide-players' and 'custom-playerlist' at the same time! (Section: custom-protocol-ping)");
            Logger.warning("You gotta choose what feature from those two you want to use.");
            Logger.warning("In order to avoid any problems, the 'custom-playerlist' feature has been temporarily disabled.");
        }
    }
}
