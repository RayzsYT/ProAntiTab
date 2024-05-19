package de.rayzs.pat.api.communication.impl;

import java.io.*;

import de.rayzs.pat.utils.DataConverter;
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
            try { serverInfo.sendData(CHANNEL_NAME, DataConverter.convertToBytes(packet));
            } catch (Throwable throwable) { throwable.printStackTrace(); }
        }
    }

    @EventHandler
    public void onQueryReceive(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase(CHANNEL_NAME)) return;
        try {
            Object packetObj = DataConverter.buildFromBytes(event.getData());
            if(!DataConverter.isPacket(packetObj)) return;

            Server server = (Server) event.getSender();
            ClientCommunication.receiveInformation(server.getInfo().getName(), packetObj, server);
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }
}
