package de.rayzs.pat.plugin.packetanalyzer.bukkit.handlers;

import de.rayzs.pat.plugin.packetanalyzer.bukkit.BukkitPacketHandler;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.node.BukkitCommandNodeHelper;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;
public class ModernCommandsNodeHandler implements BukkitPacketHandler {

    public boolean handleIncomingPacket(Player player, CommandSender sender, Object packetObj) throws Exception {
        // Ignored
        return true;
    }

    @Override
    public boolean handleOutgoingPacket(Player player, CommandSender sender, Object packetObj) throws Exception {
        final BukkitCommandNodeHelper helper = new BukkitCommandNodeHelper(packetObj);

        SubArgument.get().getCommandNodeHandler().handleCommandNode(helper, SubArgument.get().getPlayerArgument(sender));
        return true;
    }
}
