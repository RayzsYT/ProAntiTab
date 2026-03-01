package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.plugin.system.serverbrand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.plugin.packetanalyzer.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.plugin.system.communication.BackendUpdater;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.event.player.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class BukkitPlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);

        PATEventHandler.callServerPlayersChangeEvents(sender, ServerPlayersChangeEvent.Type.JOINED);
        SubArgument.get().updateCachedPlayerNames();

        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            BackendUpdater.get().handle();
        }

        CustomServerBrand.get().preparePlayer(player);
        PermissionUtil.reloadPermissions(sender);

        if (CustomServerBrand.get().isEnabled()) {
            CustomServerBrand.get().sendBrandToPlayer(player);
        }

        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            if (Storage.getPermissionPlugin() != PermissionPlugin.LUCKPERMS) {

                PATScheduler.createScheduler(() -> {

                    if (Storage.getPermissionPlugin() == PermissionPlugin.GROUPMANAGER) {
                        PermissionUtil.reloadPermissions(sender);
                    }

                    Storage.getLoader().updateCommands(sender);
                }, 20);

            }
        }

        if (!BukkitPacketAnalyzer.inject(player)) {
            PATScheduler.createScheduler(() -> {
                if (player.isOnline()) {
                    if (!BukkitPacketAnalyzer.inject(player)) {

                        if (Storage.ConfigSections.Settings.INJECTION_FAILED.ENABLED) {

                            Logger.info(MessageTranslator.replaceMessage(
                                    sender,
                                    Storage.ConfigSections.Settings.INJECTION_FAILED.CONSOLE_MESSAGE.get()
                            ));

                            player.kickPlayer(MessageTranslator.replaceMessage(
                                    sender,
                                    Storage.ConfigSections.Settings.INJECTION_FAILED.KICK_MESSAGE.get()
                            ));
                        }

                    }
                }
            }, 10);
        }

        if (Storage.OUTDATED && PermissionUtil.hasPermission(sender, "joinupdate")) {
            PATScheduler.createScheduler(() -> {
                if (player.isOnline()) {
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines());
                }
            }, 20);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);

        PATEventHandler.callServerPlayersChangeEvents(sender, ServerPlayersChangeEvent.Type.LEFT);
        SubArgument.get().updateCachedPlayerNames();

        BukkitPacketAnalyzer.uninject(player.getUniqueId());
        PermissionUtil.resetPermissions(player.getUniqueId());
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY == -1) {
            CustomServerBrand.get().sendBrandToPlayer(player);
        }

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_WORLD.ENABLED) {
            CommandSender sender = CommandSender.from(player);

            assert sender != null;
            PermissionUtil.reloadPermissions(sender);
            Storage.getLoader().updateCommands(sender);
        }
    }
}
