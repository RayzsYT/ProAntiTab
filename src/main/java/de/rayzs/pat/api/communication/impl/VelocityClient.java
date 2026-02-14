package de.rayzs.pat.api.communication.impl;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommunicationPackets;
import com.velocitypowered.api.event.Subscribe;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.communication.*;
import com.velocitypowered.api.proxy.*;
import java.util.UUID;

public class VelocityClient implements Client {

    private static final ProxyServer SERVER = VelocityLoader.getServer();
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    public VelocityClient() {

        if (Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED) {
            return;
        }

        SERVER.getChannelRegistrar().register(IDENTIFIER);
        SERVER.getEventManager().register(VelocityLoader.getInstance(), this);
    }

    @Override
    public void reload() {}

    @Override
    public void send(CommunicationPackets.PATPacket packet) {
        final byte[] preparedPacket = CommunicationPackets.preparePacket(packet);

        if (preparedPacket == null) {
            return;
        }


        for (RegisteredServer registeredServer : SERVER.getAllServers()) {

            try {
                registeredServer.sendPluginMessage(IDENTIFIER, preparedPacket);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    }

    @Subscribe
    public void onQueryReceive(PluginMessageEvent event) {
        if (event.getIdentifier() != IDENTIFIER) {
            return;
        }

        final ServerConnection server = (ServerConnection) event.getSource();
        final String serverName = server.getServerInfo().getName();
        final UUID clientId = Communicator.get().getClientId(serverName);

        if (!CommunicationPackets.isInitialPacket(event.getData()) && clientId == null) {
            return;
        }

        Object packetObj = CommunicationPackets.readPacket(event.getData(), clientId);

        if (!CommunicationPackets.isB2PPacket(packetObj)) {
            return;
        }

        Communicator.get().handleB2PPacket(serverName, (CommunicationPackets.PATPacket) packetObj);

    }

    public static MinecraftChannelIdentifier getIdentifier() {
        return IDENTIFIER;
    }
}
