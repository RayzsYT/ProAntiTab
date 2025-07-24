package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.brand.impl.BukkitServerBrand;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.Bukkit;

public class BukkitPlayerConnectionListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.JOINED);

        if(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            BackendUpdater.handle();

        PermissionUtil.setPlayerPermissions(player.getUniqueId());
        CustomServerBrand.preparePlayer(player);

        if(CustomServerBrand.isEnabled())
            CustomServerBrand.sendBrandToPlayer(player);

        if(!BukkitPacketAnalyzer.inject(player)) player.kickPlayer("Failed to inject player!");

        if(Storage.OUTDATED && PermissionUtil.hasPermission(player, "joinupdate")) {
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
        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.LEFT);

        BukkitPacketAnalyzer.uninject(player.getUniqueId());
        PermissionUtil.resetPermissions(player.getUniqueId());
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;

        CustomServerBrand.sendBrandToPlayer(player);
    }
}
