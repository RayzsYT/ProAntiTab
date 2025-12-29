package de.rayzs.pat.api.netty.bukkit.handlers;

import de.rayzs.pat.api.netty.bukkit.BukkitPacketHandler;
import de.rayzs.pat.plugin.modules.SubArgsModule;
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

        SubArgsModule.handleCommandNode(player.getUniqueId(), helper);
        return true;
    }
}
