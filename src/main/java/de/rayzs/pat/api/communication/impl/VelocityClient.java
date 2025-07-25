package de.rayzs.pat.api.communication.impl;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.utils.CommunicationPackets;
import com.velocitypowered.api.event.Subscribe;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.communication.*;
import com.velocitypowered.api.proxy.*;

public class VelocityClient implements Client {

    private static final ProxyServer SERVER = VelocityLoader.getServer();
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    public VelocityClient() {
        SERVER.getChannelRegistrar().register(IDENTIFIER);
        SERVER.getEventManager().register(VelocityLoader.getInstance(), this);
    }

    @Override
    public void send(Object packet) {
        for (RegisteredServer registeredServer : SERVER.getAllServers()) {

            try {

                registeredServer.sendPluginMessage(IDENTIFIER, CommunicationPackets.convertToBytes(packet));

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
    }

    @Subscribe
    public void onQueryReceive(PluginMessageEvent event) {
        if (event.getIdentifier() != IDENTIFIER)
            return;

        Object packetObj = CommunicationPackets.buildFromBytes(event.getData());

        if (!CommunicationPackets.isPacket(packetObj))
            return;

        ServerConnection server = (ServerConnection) event.getSource();
        Communicator.receiveInformation(server.getServerInfo().getName(), packetObj);
    }

    public static MinecraftChannelIdentifier getIdentifier() {
        return IDENTIFIER;
    }
}
