package de.rayzs.pat.plugin.listeners;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.netty.PacketAnalyzer;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class BukkitPlayerConnectionListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(!PacketAnalyzer.inject(player) && Reflection.getMinor() <= 17) player.kickPlayer("Failed to inject player!");

        if(Storage.OUTDATED_VERSION && (PermissionUtil.hasPermission(player, "update"))) {
            Bukkit.getScheduler().runTaskLater(BukkitLoader.getPlugin(), () -> {
                if(player.isOnline()) {
                    player.sendMessage("§8[§4ProAntiTab§8] §cYou're using an outdated version of this plugin!");
                    player.sendMessage("§8[§4ProAntiTab§8] §cPlease update it on: https://www.rayzs.de/products/proantitab/page");
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
