package de.rayzs.pat.api.communication.client.impl;

import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.api.communication.Client;
import de.rayzs.pat.utils.CommunicationPackets;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class BungeeClientInfo extends ClientInfo {

    public BungeeClientInfo(UUID id) {
        super(id);
    }

    public BungeeClientInfo(UUID id, String name) {
        super(id, name);
    }

    @Override
    public void send(CommunicationPackets.PATPacket packet) {
        final byte[] preparedPacket = CommunicationPackets.preparePacket(packet, getId());

        if (preparedPacket == null) {
            return;
        }

        final ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(getServerName());

        if (serverInfo != null) {
            serverInfo.sendData(Client.CHANNEL_NAME, preparedPacket);
        }
    }
}

