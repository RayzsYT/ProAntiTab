package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BungeeLoader;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.*;

public class BungeePingListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyPing(ProxyPingEvent event) {
        if(!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;

        ProxyServer proxyServer = BungeeLoader.getPlugin().getProxy();
        int online = proxyServer.getOnlineCount(),
                onlineExtend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT,
                max = proxyServer.getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
        ServerPing serverPing = event.getResponse();
        ServerPing.Protocol newProtocol = new ServerPing.Protocol(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL.replace("%online_extended%", String.valueOf(onlineExtend)).replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max)), !Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW ? serverPing.getVersion().getProtocol() : 0);
        serverPing.setVersion(newProtocol);

        event.setResponse(serverPing);
    }
}
