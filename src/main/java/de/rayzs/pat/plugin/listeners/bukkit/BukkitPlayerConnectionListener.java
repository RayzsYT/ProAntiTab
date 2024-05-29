package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.brand.impl.BukkitServerBrand;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.api.netty.PacketAnalyzer;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.utils.message.MessageTranslator;
import org.bukkit.Bukkit;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class BukkitPlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            BackendUpdater.handle();

        CustomServerBrand.preparePlayer(player);
        CustomServerBrand.sendBrandToPlayer(player);

        if(!PacketAnalyzer.inject(player)) player.kickPlayer("Failed to inject player!");

        if(!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && Storage.OUTDATED && (PermissionUtil.hasPermission(player, "update"))) {
            Bukkit.getScheduler().runTaskLater(BukkitLoader.getPlugin(), () -> {
                if(player.isOnline()) {
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines());
                }
            }, 20);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PacketAnalyzer.uninject(player.getUniqueId());
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        BukkitServerBrand.removeFromModified(player);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        BukkitServerBrand.removeFromModified(player);
    }
}
