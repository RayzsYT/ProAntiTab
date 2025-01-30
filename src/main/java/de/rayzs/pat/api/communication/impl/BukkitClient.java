package de.rayzs.pat.api.communication.impl;

import de.rayzs.pat.utils.scheduler.PATScheduler;
import org.bukkit.plugin.messaging.PluginMessageListener;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.communication.*;
import org.bukkit.entity.Player;
import org.bukkit.*;

public class BukkitClient implements Client, PluginMessageListener {

    private static final Server SERVER = Bukkit.getServer();

    public BukkitClient() {
        SERVER.getMessenger().registerIncomingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME, this);
        SERVER.getMessenger().registerOutgoingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME);
    }

    @Override
    public void send(Object packet) {
        try { SERVER.sendPluginMessage(BukkitLoader.getPlugin(), CHANNEL_NAME, CommunicationPackets.convertToBytes(packet));
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if(!channel.equals(CHANNEL_NAME)) return;
        try {
            Object packetObj = CommunicationPackets.buildFromBytes(bytes);
            if(!CommunicationPackets.isPacket(packetObj)) return;

            PATScheduler.createScheduler(() -> Communicator.receiveInformation("proxy", packetObj));
        } catch (Throwable throwable) { throwable.printStackTrace(); }
    }
}
