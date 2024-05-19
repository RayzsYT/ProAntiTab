package de.rayzs.pat.api.communication.impl;

import org.bukkit.*;
import com.google.common.io.*;
import org.bukkit.entity.Player;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.communication.*;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class BukkitClient implements Client, PluginMessageListener {

    private static final Server SERVER = Bukkit.getServer();

    public BukkitClient() {
        SERVER.getMessenger().registerIncomingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME, this);
        SERVER.getMessenger().registerOutgoingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME);
    }

    @Override
    public void sendInformation(String information) {
        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(information);
            SERVER.sendPluginMessage(BukkitLoader.getPlugin(), CHANNEL_NAME, out.toByteArray());
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if(!channel.equals(CHANNEL_NAME)) return;
        try {
            ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
            String information = input.readUTF();
            Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitLoader.getPlugin(), () -> ClientCommunication.receiveInformation("proxy", information));
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }
}
