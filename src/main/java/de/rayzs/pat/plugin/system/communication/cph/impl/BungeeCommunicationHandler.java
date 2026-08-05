package de.rayzs.pat.plugin.system.communication.cph.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.plugin.system.communication.cph.CommunicationPacketHandler;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.hooks.LuckPermsHook;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.md_5.bungee.api.ProxyServer;
import java.util.UUID;

public class BungeeCommunicationHandler implements CommunicationPacketHandler {

    @Override
    public void handleReceivedPacket(CommunicationPackets.PATPacket incomingPacket) {

        if (Storage.getPermissionPlugin() == PermissionPlugin.LUCKPERMS) {

            if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.AnnouncePlayerPermissionChanges packet) {
                ProxyServer.getInstance().getScheduler().runAsync(BungeeLoader.getPlugin(), () -> {
                    LuckPermsHook.forcePermissionRecalculation(packet.playerId());
                    PermissionUtil.reloadPermissions(packet.playerId());
                });

                return;
            }

            if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.AnnounceGroupPermissionChanges packet) {
                ProxyServer.getInstance().getScheduler().runAsync(BungeeLoader.getPlugin(), () -> {
                    for (UUID playerId : packet.playerIds()) {
                        LuckPermsHook.forcePermissionRecalculation(playerId);
                        PermissionUtil.reloadPermissions(playerId);
                    }
                });
            }
        }
    }
}
