package de.rayzs.pat.plugin.packetanalyzer.bukkit.handlers;

import de.rayzs.pat.plugin.packetanalyzer.bukkit.BukkitPacketHandler;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.node.BukkitCommandNodeHelper;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;

public class ModernCommandsNodeHandler implements BukkitPacketHandler {

    public boolean handleIncomingPacket(
            final Player player,
            final CommandSender sender,
            final Object packetObj,
            final boolean isOperator
    ) {
        // Ignored
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(
            final Player player,
            final CommandSender sender,
            final Object packetObj,
            final boolean isOperator
    ) throws Exception {
        final BukkitCommandNodeHelper helper = new BukkitCommandNodeHelper(packetObj);

        SubArgument.get().getCommandNodeHandler().handleCommandNode(
                helper,
                SubArgument.get().getPlayerArgument(sender)
        );

        return true;
    }
}
