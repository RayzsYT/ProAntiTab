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
        if(!Storage.CANCEL_COMMANDS) return;

        Player player = event.getPlayer();
        String rawCommand = event.getMessage(), command = rawCommand.replaceFirst("/", ""), commandLowerCased = command.toLowerCase();

        if(Storage.USE_UNKNOWN_COMMAND) {
            String targetCommand = rawCommand.contains(" ") ? rawCommand.split(" ")[0] : rawCommand;
            HelpTopic helpTopic = Bukkit.getHelpMap().getHelpTopic(targetCommand);
            if(helpTopic == null) {
                for (String line : Storage.UNKNOWN_COMMAND) MessageTranslator.send(player, line.replace("&", "§"));
                event.setCancelled(true);
                return;
            }
        }

        if(rawCommand.equals("/")) return;

        if((Storage.TURN_BLACKLIST_TO_WHITELIST)) {
            if(Storage.isCommandBlockedPrecise(command)) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;
            MessageTranslator.send(player, Storage.CANCEL_COMMANDS_MESSAGE.replace("%command%", rawCommand.replaceFirst("/", "")));
            event.setCancelled(true);
            return;
        }

        if (command.contains(":")) {
            String[] commandSplit = command.split(":");
            if(commandSplit.length > 1) command = commandSplit[1];
        }

        if (!Storage.isCommandBlocked(commandLowerCased)) return;
        if (PermissionUtil.hasBypassPermission(player, command)) return;

        MessageTranslator.send(player, Storage.CANCEL_COMMANDS_MESSAGE.replace("%command%", rawCommand.replaceFirst("/", "")));

        final World world = player.getWorld();
        final String worldName = world.getName(), alertMessage = Storage.NOTIFY_ALERT.replace("%player%", player.getName()).replace("%command%", command).replace("%world%", worldName);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            Player target = Bukkit.getServer().getPlayer(uuid);
            MessageTranslator.send(target, alertMessage);
        });
        if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
        event.setCancelled(true);
    }
}
