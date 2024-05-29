 package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;

import java.util.List;

 public class BukkitBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String rawCommand = event.getMessage(), command = rawCommand.replaceFirst("/", ""), worldName = world.getName();
        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getName(), "%command%", command, "%world%", worldName);

        if(Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED) {
            String targetCommand = rawCommand.contains(" ") ? rawCommand.split(" ")[0] : rawCommand;
            HelpTopic helpTopic = Bukkit.getHelpMap().getHelpTopic(targetCommand);
            if(helpTopic == null) {
                MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE);
                event.setCancelled(true);
                return;
            }
        }

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isPluginsCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", rawCommand.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                Player target = Bukkit.getServer().getPlayer(uuid);
                MessageTranslator.send(target, notificationMessage);
            });
            event.setCancelled(true);
            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED || Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED || rawCommand.equals("/")) return;
        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", rawCommand.replaceFirst("/", ""));

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.isListed(command, true)) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;
            MessageTranslator.send(player, cancelCommandMessage);
            event.setCancelled(true);
            return;
        }

        if (!Storage.Blacklist.isBlocked(player, command)) return;
        if (PermissionUtil.hasBypassPermission(player, command)) return;
        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            Player target = Bukkit.getServer().getPlayer(uuid);
            MessageTranslator.send(target, notificationMessage);
        });
        event.setCancelled(true);
    }
}
