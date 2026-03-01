package de.rayzs.pat.plugin.system.communication.client.impl;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.plugin.system.communication.client.Client;
import de.rayzs.pat.plugin.system.communication.pmc.impl.VelocityPluginMessageClient;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.CommunicationPackets;

import java.util.Optional;
import java.util.UUID;

public class VelocityClient extends Client {

    public VelocityClient(UUID id, String name) {
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
            registeredServer.sendPluginMessage(VelocityPluginMessageClient.getIdentifier(), preparedPacket);
        });
    }
}
