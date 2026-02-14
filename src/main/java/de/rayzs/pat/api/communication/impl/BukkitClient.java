package de.rayzs.pat.api.communication.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import org.bukkit.plugin.messaging.PluginMessageListener;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.communication.*;
import org.bukkit.entity.Player;
import org.bukkit.*;

public class BukkitClient implements Client, PluginMessageListener {

    private static final Server SERVER = Bukkit.getServer();
    private boolean registered = false;

    public BukkitClient() {
        reload();
    }

    @Override
    public void reload() {
        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {

            if (registered) {
                unregister();
            }

            return;
        }

        if (!registered) {
            register();
        }
    }

    private void register() {
        registered = true;

        SERVER.getMessenger().registerIncomingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME, this);
        SERVER.getMessenger().registerOutgoingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME);
    }

    private void unregister() {
        registered = false;

        SERVER.getMessenger().unregisterIncomingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME, this);
        SERVER.getMessenger().unregisterOutgoingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME);
    }

    @Override
    public void send(CommunicationPackets.PATPacket packet) {
        final byte[] preparedPacket = CommunicationPackets.preparePacket(packet);

        if (preparedPacket == null) {
            return;
        }


        try {
            SERVER.sendPluginMessage(BukkitLoader.getPlugin(), CHANNEL_NAME, preparedPacket);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {

        if (!channel.equals(CHANNEL_NAME)) {
            return;
        }

        try {
            final Object packetObj = CommunicationPackets.readPacket(bytes);

            if (!CommunicationPackets.isP2BPacket(packetObj)) {
                return;
            }

            PATScheduler.createScheduler(() -> Communicator.get().handleP2BPacket((CommunicationPackets.PATPacket) packetObj));

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
