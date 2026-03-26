package de.rayzs.pat.plugin.system.communication.pmc.impl;

import de.rayzs.pat.plugin.system.communication.Communicator;
import de.rayzs.pat.plugin.system.communication.pmc.PluginMessageClient;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import org.bukkit.plugin.messaging.PluginMessageListener;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.entity.Player;
import org.bukkit.*;

import java.util.Iterator;

public class BukkitPluginMessageClient implements PluginMessageClient, PluginMessageListener {

    private static final Server SERVER = Bukkit.getServer();
    private boolean registered = false;

    public BukkitPluginMessageClient() {
        reload();
    }

    @Override
    public void reload() {
        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            register();
        } else {
            unregister();
        }
    }

    private void register() {
        if (registered) {
            return;
        }


        registered = true;
        SERVER.getMessenger().registerIncomingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME, this);
        SERVER.getMessenger().registerOutgoingPluginChannel(BukkitLoader.getPlugin(), CHANNEL_NAME);
    }

    private void unregister() {

        if (!registered) {
            return;
        }


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


        final Player carrier = selectCarrier();
        if (carrier == null) {
            return;
        }

        PATScheduler.execute(() -> {
            if (!carrier.isOnline()) {
                return;
            }

            try {
                carrier.sendPluginMessage(BukkitLoader.getPlugin(), CHANNEL_NAME, preparedPacket);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }, carrier);
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

    private Player selectCarrier() {
        final Iterator<? extends Player> iterator = SERVER.getOnlinePlayers().iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
}
