 package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.*;
import de.rayzs.pat.utils.message.MessageTranslator;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.help.HelpTopic;

public class BukkitBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String rawCommand = event.getMessage(), command = rawCommand.replaceFirst("/", ""), commandLowerCased = command.toLowerCase(),
                worldName = world.getName(), alertMessage = Storage.NOTIFY_ALERT.replace("%player%", player.getName()).replace("%command%", command).replace("%world%", worldName);


        if(Storage.USE_UNKNOWN_COMMAND) {
            String targetCommand = rawCommand.contains(" ") ? rawCommand.split(" ")[0] : rawCommand;
            HelpTopic helpTopic = Bukkit.getHelpMap().getHelpTopic(targetCommand);
            if(helpTopic == null) {
                for (String line : Storage.UNKNOWN_COMMAND) MessageTranslator.send(player, line.replace("&", "§"));
                event.setCancelled(true);
                return;
            }
        }

        if(Storage.isPluginsCommand(command) && Storage.USE_CUSTOM_PLUGINS && !PermissionUtil.hasBypassPermission(player, command)) {
            for (String line : Storage.CUSTOM_PLUGINS) MessageTranslator.send(player, line.replace("%command%", rawCommand.replaceFirst("/", "")));

            if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                Player target = Bukkit.getServer().getPlayer(uuid);
                MessageTranslator.send(target, alertMessage);
            });
            event.setCancelled(true);
            return;
        }

        if(!Storage.CANCEL_COMMANDS || Storage.BUNGEECORD || rawCommand.equals("/")) return;

        if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
            if(Storage.isBlocked(command, true)) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;
            for (String line : Storage.CANCEL_COMMANDS_MESSAGE) MessageTranslator.send(player, line.replace("%command%", rawCommand.replaceFirst("/", "")));
            event.setCancelled(true);
            return;
        }

        if (!Storage.isBlocked(commandLowerCased, false)) return;
        if (PermissionUtil.hasBypassPermission(player, command)) return;

        for (String line : Storage.CANCEL_COMMANDS_MESSAGE) MessageTranslator.send(player, line.replace("%command%", rawCommand.replaceFirst("/", "")));

        if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            Player target = Bukkit.getServer().getPlayer(uuid);
            MessageTranslator.send(target, alertMessage);
        });
        event.setCancelled(true);
    }
}
