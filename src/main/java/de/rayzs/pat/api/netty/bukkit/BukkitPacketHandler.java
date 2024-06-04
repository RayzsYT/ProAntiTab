package de.rayzs.pat.api.netty.bukkit;

import org.bukkit.entity.Player;

public interface BukkitPacketHandler {
    boolean handleIncomingPacket(Player player, Object packetObj) throws Exception;
    boolean handleOutgoingPacket(Player player, Object packetObj) throws Exception;
}
