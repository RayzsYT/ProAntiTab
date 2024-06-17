package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import de.rayzs.pat.api.storage.Storage;

public class VelocityPingListener {

    private static ProxyServer server;

    public VelocityPingListener(ProxyServer server) {
        VelocityPingListener.server = server;
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if(!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ENABLED) return;

        int online = server.getPlayerCount(),
                onlineExtend = online + Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.EXTEND_COUNT,
                max = server.getConfiguration().getShowMaxPlayers();
        ServerPing oldServerPing = event.getPing(), newServerPing;
        ServerPing.Version newVersion = new ServerPing.Version(!Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.ALWAYS_SHOW ? oldServerPing.getVersion().getProtocol() : -1, Storage.ConfigSections.Settings.CUSTOM_PROTOCOL_PING.PROTOCOL.replace("%online_extended%", String.valueOf(onlineExtend)).replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max)));
        newServerPing = oldServerPing.asBuilder().version(newVersion).build();

        event.setPing(newServerPing);
    }
}
