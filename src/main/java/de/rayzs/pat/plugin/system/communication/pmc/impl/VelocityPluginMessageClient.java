package de.rayzs.pat.plugin.system.communication.pmc.impl;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.plugin.system.communication.Communicator;
import de.rayzs.pat.plugin.system.communication.pmc.PluginMessageClient;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommunicationPackets;
import com.velocitypowered.api.event.Subscribe;
import de.rayzs.pat.plugin.VelocityLoader;
import com.velocitypowered.api.proxy.*;
import java.util.UUID;

public class VelocityPluginMessageClient implements PluginMessageClient {

    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);
    private boolean registered = false;

    public VelocityPluginMessageClient() {
        reload();
    }

    @Override
    public void reload() {
        if (!Storage.ConfigSections.Settings.DISABLE_SYNC.ENABLED) {
            register();
        } else {
            unregister();
        }
    }

    private void register() {
        if (registered) {
            return;
        }


        registered = true;

        VelocityLoader.getServer().getChannelRegistrar().register(IDENTIFIER);
        VelocityLoader.getServer().getEventManager().register(VelocityLoader.getInstance(), this);
    }

    private void unregister() {
        if (registered) {
            return;
        }


        registered = true;

        VelocityLoader.getServer().getChannelRegistrar().unregister(IDENTIFIER);
        VelocityLoader.getServer().getEventManager().unregisterListener(VelocityLoader.getInstance(), this);
    }

    @Override
    public void send(CommunicationPackets.PATPacket packet) {
        final byte[] preparedPacket = CommunicationPackets.preparePacket(packet);

        if (preparedPacket == null) {
            return;
        }


        for (RegisteredServer registeredServer : VelocityLoader.getServer().getAllServers()) {

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
