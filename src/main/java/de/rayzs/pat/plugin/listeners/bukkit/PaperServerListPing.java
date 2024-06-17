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

        int online = Bukkit.getOnlinePlayers().size(), max = Bukkit.getMaxPlayers();
        String versionName = Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL.replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max));
        event.setHidePlayers(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.HIDE_PLAYERS);
        event.setVersion(versionName);
    }
}
