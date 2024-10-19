package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.plugin.logger.Logger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeePingListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyPing(ProxyPingEvent event) {
        if (!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;

        ProxyServer proxyServer = BungeeLoader.getPlugin().getProxy();
        int online = proxyServer.getOnlineCount(),
                onlineExtend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT,
                max = -1;

        try {
            max = proxyServer.getConfigurationAdapter().getListeners().iterator().next().getMaxPlayers();
        } catch (Throwable throwable) {
            Logger.debug("Failed to read max-players count for %max% placeholder! Using -1 as default value instead.");
        }

        ServerPing serverPing = event.getResponse();
        ServerPing.Protocol newProtocol = new ServerPing.Protocol(Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL.replace("%online_extended%", String.valueOf(onlineExtend)).replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max)), !Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW ? serverPing.getVersion().getProtocol() : 0);
        serverPing.setVersion(newProtocol);

        if (Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.USE_EXTEND_AS_MAX_COUNT)
            serverPing.getPlayers().setMax(onlineExtend);

        event.setResponse(serverPing);
    }
}
