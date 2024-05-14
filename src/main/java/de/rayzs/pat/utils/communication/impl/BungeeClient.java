package de.rayzs.pat.utils.communication.impl;

import java.io.*;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.communication.*;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import org.bukkit.Server;

public class BungeeClient implements Client, Listener {

    private static final ProxyServer SERVER = ProxyServer.getInstance();

    public BungeeClient() {
        SERVER.registerChannel(CHANNEL_NAME);
        SERVER.getPluginManager().registerListener(BungeeLoader.getPlugin(), this);
    }

    @Override
    public void sendInformation(String information) {
        for(ServerInfo serverInfo : ProxyServer.getInstance().getServers().values()) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                out.writeUTF(information);
                serverInfo.sendData(CHANNEL_NAME, stream.toByteArray());
            } catch (Throwable throwable) { throwable.printStackTrace(); }
        }
    }

    @EventHandler
    public void onQueryReceive(PluginMessageEvent event) {
        if (!event.getTag().equalsIgnoreCase(CHANNEL_NAME)) return;
        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(event.getData()));
            String information = input.readUTF();
            Server server = (Server) event.getSender();
            ClientCommunication.receiveInformation(server.getName(), information);
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }
}
