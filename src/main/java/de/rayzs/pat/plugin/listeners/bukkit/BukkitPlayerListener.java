package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BukkitPlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        final CommandSender sender = CommandSenderHandler.from(player);
        sender.updateSenderObject(player);

        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.JOINED);

        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            BackendUpdater.handle();
        }

        CustomServerBrand.preparePlayer(player);
        PermissionUtil.reloadPermissions(sender);

        if (CustomServerBrand.isEnabled()) {
            CustomServerBrand.sendBrandToPlayer(player);
        }

        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            if (Storage.getPermissionPlugin() != PermissionPlugin.LUCKPERMS) {

                PATScheduler.createScheduler(() -> {

                    if (Storage.getPermissionPlugin() == PermissionPlugin.GROUPMANAGER) {
                        PermissionUtil.reloadPermissions(sender);
                    } else {
                        sender.updateGroups();
                    }

                    if (Reflection.getMinor() >= 13) {
                        BukkitAntiTabListener.handleTabCompletion(player);
                    }
                }, 20);

            }
        }

        if (!BukkitPacketAnalyzer.inject(player)) {
            PATScheduler.createScheduler(() -> {
                if (player.isOnline()) {
                    if (!BukkitPacketAnalyzer.inject(player)) {
                        Logger.warning("Failed to inject into player! (" + player.getName() + ")" );
                        player.kickPlayer("Failed to inject player!");
                    }
                }
            }, 10);
        }

        // Update sub arguments for <1.12.2 servers.
        if (Reflection.getMinor() < 13) {
            Storage.quickSubArgumentUpdate(uuid);
        }

        if (Storage.OUTDATED && PermissionUtil.hasPermission(player, "joinupdate")) {
            PATScheduler.createScheduler(() -> {
                if (player.isOnline()) {
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
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            CustomServerBrand.sendBrandToPlayer(player);
        }

        if (Storage.ConfigSections.Settings.UPDATE_COMMANDS_PER_WORLD.ENABLED) {
            CommandSender sender = CommandSenderHandler.from(player);

            assert sender != null;
            PermissionUtil.reloadPermissions(sender);

            if (Reflection.getMinor() >= 13) {
                BukkitAntiTabListener.handleTabCompletion(player);
            }
        }
    }
}
