package de.rayzs.pat.plugin.packetanalyzer.bukkit;

import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;

public interface BukkitPacketHandler {
    boolean handleIncomingPacket(
            final Player player,
            final CommandSender sender,
            final Object packetObj,
            final boolean isOperator
    ) throws Exception;

    boolean handleOutgoingPacket(
            final Player player,
            final CommandSender sender,
            final Object packetObj,
            final boolean isOperator
    ) throws Exception;
}
