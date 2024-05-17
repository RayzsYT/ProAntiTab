package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.Storage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.*;

public class BungeePingListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyPing(ProxyPingEvent event) {
        if(!Storage.USE_CUSTOM_PROTOCOL_PING) return;

        ProxyServer proxyServer = BungeeLoader.getPlugin().getProxy();
        int online = proxyServer.getOnlineCount(), max = proxyServer.getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
        ServerPing serverPing = event.getResponse();
        ServerPing.Protocol newProtocol = new ServerPing.Protocol(Storage.CUSTOM_PROTOCOL_PING.replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max)), !Storage.USE_ALWAYS_SHOW_CUSTOM_PROTOCOL_PING ? serverPing.getVersion().getProtocol() : 0);
        serverPing.setVersion(newProtocol);

        event.setResponse(serverPing);
    }
}
