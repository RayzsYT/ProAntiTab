package de.rayzs.pat.plugin.system.communication.cph.impl;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.plugin.system.communication.cph.CommunicationPacketHandler;
import de.rayzs.pat.utils.CommunicationPackets;
import de.rayzs.pat.utils.hooks.LuckPermsHook;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.permission.PermissionUtil;
import java.util.UUID;

public class VelocityCommunicationHandler implements CommunicationPacketHandler {

    @Override
    public void handleReceivedPacket(CommunicationPackets.PATPacket incomingPacket) {

        if (Storage.getPermissionPlugin() == PermissionPlugin.LUCKPERMS) {

            if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.AnnouncePlayerPermissionChanges packet) {
                VelocityLoader.getServer().getScheduler().buildTask(VelocityLoader.getInstance(), task -> {
                    LuckPermsHook.forcePermissionRecalculation(packet.playerId());
                    PermissionUtil.reloadPermissions(packet.playerId());
                }).schedule();
            }

            if (incomingPacket instanceof CommunicationPackets.Backend2Proxy.AnnounceGroupPermissionChanges packet) {
                VelocityLoader.getServer().getScheduler().buildTask(VelocityLoader.getInstance(), task -> {
                    for (UUID playerId : packet.playerIds()) {
                        if (Storage.getPermissionPlugin() == PermissionPlugin.LUCKPERMS) {
                            LuckPermsHook.forcePermissionRecalculation(playerId);
                            PermissionUtil.reloadPermissions(playerId);
                        }
                    }
                }).schedule();
            }
        }
    }
}
