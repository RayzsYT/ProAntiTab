package de.rayzs.pat.plugin.system.communication.client.impl;

import de.rayzs.pat.plugin.system.communication.client.Client;
import de.rayzs.pat.plugin.system.communication.pmc.PluginMessageClient;
import de.rayzs.pat.utils.CommunicationPackets;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class BungeeClient extends Client {

    public BungeeClient(UUID id, String name) {
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
            serverInfo.sendData(PluginMessageClient.CHANNEL_NAME, preparedPacket);
        }
    }
}

