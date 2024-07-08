package de.rayzs.pat.plugin.listeners.bukkit;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import java.util.List;
import org.bukkit.*;

public class BukkitBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onUnknownCommandRecognition(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String rawCommand = StringUtils.getFirstArg(event.getMessage()), command =  StringUtils.replaceFirst(rawCommand, "/", "");

        if(!Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED || event.isCancelled()) return;
        if(BukkitLoader.doesCommandExist(command, false)) return;
        if(Bukkit.getHelpMap().getHelpTopic(rawCommand) != null) return;

        event.setCancelled(true);
        MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE, "%command%", command, "%world%", world.getName());
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String rawCommand = event.getMessage(), command = rawCommand, worldName = world.getName();

        command = StringUtils.replaceFirst(command, "/", "");
        command = StringUtils.getFirstArg(command);
        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");

        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getName(), "%command%", command, "%world%", worldName);

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            event.setCancelled(true);
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE,  "%command%", command);

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                Player target = Bukkit.getServer().getPlayer(uuid);
                MessageTranslator.send(target, notificationMessage);
            });

            return;
        }

        if(Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            event.setCancelled(true);
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE,  "%command%", command);

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                Player target = Bukkit.getServer().getPlayer(uuid);
                MessageTranslator.send(target, notificationMessage);
            });

            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED || Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED || rawCommand.equals("/")) return;
        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", command);

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.isListed(command, true)) return;
            if(PermissionUtil.hasBypassPermission(player, command, false)) return;
            event.setCancelled(true);
            MessageTranslator.send(player, cancelCommandMessage);
            return;
        }

        if (!Storage.Blacklist.isListed(command, false)) return;
        if (PermissionUtil.hasBypassPermission(player, command, false)) return;
        event.setCancelled(true);
        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            Player target = Bukkit.getServer().getPlayer(uuid);
            MessageTranslator.send(target, notificationMessage);
        });
    }
}
