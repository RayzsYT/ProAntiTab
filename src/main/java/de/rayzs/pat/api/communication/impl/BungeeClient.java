package de.rayzs.pat.api.communication.impl;

import de.rayzs.pat.utils.CommunicationPackets;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.api.communication.*;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;

public class BungeeClient implements Client, Listener {

    private static final ProxyServer SERVER = ProxyServer.getInstance();

    public BungeeClient() {
        SERVER.registerChannel(CHANNEL_NAME);
        SERVER.getPluginManager().registerListener(BungeeLoader.getPlugin(), this);
    }

    @Override
    public void send(Object packet) {
        for(ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            try { serverInfo.sendData(CHANNEL_NAME, CommunicationPackets.convertToBytes(packet));
            } catch (Throwable throwable) { throwable.printStackTrace(); }
        }
    }

    @EventHandler
    public void onQueryReceive(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase(CHANNEL_NAME)) return;
        try {
            Object packetObj = CommunicationPackets.buildFromBytes(event.getData());
            if(!CommunicationPackets.isPacket(packetObj)) return;

            Server server = (Server) event.getSender();
            Communicator.receiveInformation(server.getInfo().getName().toLowerCase(), packetObj);
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }
}
