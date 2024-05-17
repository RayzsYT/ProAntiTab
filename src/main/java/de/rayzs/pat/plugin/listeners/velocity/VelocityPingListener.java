package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.Storage;

public class VelocityPingListener {

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if(!Storage.USE_CUSTOM_PROTOCOL_PING) return;

        ProxyServer proxyServer = VelocityLoader.getServer();
        int online = proxyServer.getPlayerCount(), max = proxyServer.getConfiguration().getShowMaxPlayers();
        ServerPing oldServerPing = event.getPing(), newServerPing;
        ServerPing.Version newVersion = new ServerPing.Version(!Storage.USE_ALWAYS_SHOW_CUSTOM_PROTOCOL_PING ? oldServerPing.getVersion().getProtocol() : -1, Storage.CUSTOM_PROTOCOL_PING.replace("%online%", String.valueOf(online)).replace("%max%", String.valueOf(max)));
        newServerPing = oldServerPing.asBuilder().version(newVersion).build();

        event.setPing(newServerPing);
    }
}
