package de.rayzs.pat.api.communication.client.impl;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.Client;
import net.md_5.bungee.api.ProxyServer;

public class BungeeClientInfo extends ClientInfo {

    public BungeeClientInfo(String serverId) {
        super(serverId);
    }

    public BungeeClientInfo(String serverId, String name) {
        super(serverId, name);
    }

    @Override
    public void sendBytes(byte[] bytes) {
        ProxyServer.getInstance().getServers().entrySet().stream().filter(entry ->
                entry.getKey().equalsIgnoreCase(getName())
        ).forEach(entry ->
                entry.getValue().sendData(Client.CHANNEL_NAME, bytes)
        );
    }
}

