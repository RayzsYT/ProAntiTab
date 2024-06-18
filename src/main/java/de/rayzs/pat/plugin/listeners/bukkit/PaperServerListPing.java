package de.rayzs.pat.plugin.listeners.bukkit;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import de.rayzs.pat.api.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.event.*;

public class PaperServerListPing implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPaperServerListPing(PaperServerListPingEvent event) {
        if(!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;
        if(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW) event.setProtocolVersion(0);

        int online = Bukkit.getOnlinePlayers().size(),
                onlineExtend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT,
                max = Bukkit.getMaxPlayers();

        if(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_EXTEND_AS_MAX_COUNT)
            event.setMaxPlayers(onlineExtend);

        String versionName = Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL.replace("%online_extended%", String.valueOf(onlineExtend)).replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max));
        if(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.HIDE_PLAYERS)
            event.getPlayerSample().clear();

        event.setVersion(versionName);
    }
}
