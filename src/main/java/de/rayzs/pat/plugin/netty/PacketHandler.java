package de.rayzs.pat.plugin.netty;

import org.bukkit.entity.Player;

public interface PacketHandler {

    void handlePacket(Player player, Object packetObj) throws Exception;
}
