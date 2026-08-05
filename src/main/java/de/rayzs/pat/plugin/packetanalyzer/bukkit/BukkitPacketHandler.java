package de.rayzs.pat.plugin.packetanalyzer.bukkit;

import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;

public interface BukkitPacketHandler {
    boolean handleIncomingPacket(Player player, CommandSender sender, Object packetObj) throws Exception;
    boolean handleOutgoingPacket(Player player, CommandSender sender, Object packetObj) throws Exception;
}
