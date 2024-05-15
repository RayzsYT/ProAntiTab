package de.rayzs.pat.utils.communication.impl;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.communication.Client;
import de.rayzs.pat.utils.communication.ClientCommunication;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class VelocityClient implements Client {

    private static final ProxyServer SERVER = VelocityLoader.getServer();
    private static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(CHANNEL_NAME);

    public VelocityClient() {
        SERVER.getChannelRegistrar().register(IDENTIFIER);
        SERVER.getEventManager().register(VelocityLoader.getInstance(), this);
    }

    @Override
    public void sendInformation(String information) {
        for (RegisteredServer registeredServer : SERVER.getAllServers()) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(stream);
                out.writeUTF(information);
                registeredServer.sendPluginMessage(IDENTIFIER, stream.toByteArray());
            } catch (Throwable throwable) { throwable.printStackTrace(); }
        }
    }

    @Subscribe
    public void onQueryReceive(PluginMessageEvent event) {
        if (event.getIdentifier() != IDENTIFIER) return;

        try {
            DataInputStream input = new DataInputStream(new ByteArrayInputStream(event.getData()));
            String information = input.readUTF();
            ServerConnection server = (ServerConnection) event.getSource();
            ClientCommunication.receiveInformation(server.getServerInfo().getName(), information);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
