package de.rayzs.pat.plugin.netty;

import org.bukkit.entity.Player;

import java.util.HashMap;

public interface PacketHandler {
    HashMap<Player, String> playerInputCache = new HashMap<>();
    boolean handleIncomingPacket(Player player, Object packetObj) throws Exception;
    boolean handleOutgoingPacket(Player player, Object packetObj) throws Exception;
}
