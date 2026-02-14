package de.rayzs.pat.api.communication.impl;

import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.event.PluginMessageEvent;
import de.rayzs.pat.utils.CommunicationPackets;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.api.communication.*;
import net.md_5.bungee.api.ProxyServer;

import java.util.UUID;

public class BungeeClient implements Client, Listener {

    private static final ProxyServer SERVER = ProxyServer.getInstance();

    public BungeeClient() {

        if (Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED) {
            return;
        }

        SERVER.registerChannel(CHANNEL_NAME);
        SERVER.getPluginManager().registerListener(BungeeLoader.getPlugin(), this);
    }

    @Override
    public void reload() {}

    @Override
    public void send(CommunicationPackets.PATPacket packet) {
        final byte[] preparedPacket = CommunicationPackets.preparePacket(packet);

        if (preparedPacket == null) {
            return;
        }


        for (ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            try {
                serverInfo.sendData(CHANNEL_NAME, preparedPacket);
            } catch (Exception exception) {
                exception.printStackTrace();
            }

        }
    }

    @EventHandler
    public void onQueryReceive(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase(CHANNEL_NAME)) {
            return;
        }

        final Server server = (Server) event.getSender();
        final String serverName = server.getInfo().getName();
        final UUID clientId = Communicator.get().getClientId(serverName);

        if (!CommunicationPackets.isInitialPacket(event.getData()) && clientId == null) {
            return;
        }

        try {

            Object packetObj = CommunicationPackets.readPacket(event.getData(), clientId);
            if (!CommunicationPackets.isB2PPacket(packetObj)) {
                return;
            }

            Communicator.get().handleB2PPacket(serverName, (CommunicationPackets.PATPacket) packetObj);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }
}
