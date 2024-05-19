package de.rayzs.pat.api.communication.impl;

import de.rayzs.pat.utils.DataConverter;
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
    public void send(Object packet) {
        try { SERVER.sendPluginMessage(BukkitLoader.getPlugin(), CHANNEL_NAME, DataConverter.convertToBytes(packet));
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if(!channel.equals(CHANNEL_NAME)) return;
        try {
            Object packetObj = DataConverter.buildFromBytes(bytes);
            if(!DataConverter.isPacket(packetObj)) return;

            Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitLoader.getPlugin(), () -> ClientCommunication.receiveInformation("proxy", packetObj, null));
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }
}
