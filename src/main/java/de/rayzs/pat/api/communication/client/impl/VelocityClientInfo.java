package de.rayzs.pat.api.communication.client.impl;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.api.communication.impl.VelocityClient;
import de.rayzs.pat.api.communication.client.ClientInfo;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.CommunicationPackets;

import java.util.Optional;
import java.util.UUID;

public class VelocityClientInfo extends ClientInfo {

    public VelocityClientInfo(UUID id) {
        super(id);
    }

    public VelocityClientInfo(UUID id, String name) {
        super(id, name);
    }

    @Override
    public void send(CommunicationPackets.PATPacket packet) {
        final byte[] preparedPacket = CommunicationPackets.preparePacket(packet, getId());

        if (preparedPacket == null) {
            return;
        }

        final Optional<RegisteredServer> optServer = VelocityLoader.getServer().getServer(getServerName());

        optServer.ifPresent(registeredServer -> {
            registeredServer.sendPluginMessage(VelocityClient.getIdentifier(), preparedPacket);
        });
    }
}
