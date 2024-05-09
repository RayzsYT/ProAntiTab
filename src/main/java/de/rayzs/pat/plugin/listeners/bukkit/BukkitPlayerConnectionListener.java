package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.netty.PacketAnalyzer;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class BukkitPlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!PacketAnalyzer.inject(player) && Reflection.getMinor() <= 17) player.kickPlayer("Failed to inject player!");

        if(!Storage.BUNGEECORD && Storage.OUTDATED_VERSION && (PermissionUtil.hasPermission(player, "update"))) {
            Bukkit.getScheduler().runTaskLater(BukkitLoader.getPlugin(), () -> {
                if(player.isOnline()) {
                    Storage.UPDATE_NOTIFICATION.forEach(message -> player.sendMessage(message.replace("&", "§")));
                }
            }, 20);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PacketAnalyzer.uninject(player.getUniqueId());
    }
}
