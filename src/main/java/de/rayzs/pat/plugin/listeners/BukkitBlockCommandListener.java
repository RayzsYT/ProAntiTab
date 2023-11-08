package de.rayzs.pat.plugin.listeners;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class BukkitBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandProcess(PlayerCommandPreprocessEvent event) {
        if(!Storage.CANCEL_COMMANDS) return;

        Player player = event.getPlayer();
        String rawCommand = event.getMessage(), command = rawCommand.replaceFirst("/", ""), commandLowerCased = command.toLowerCase();
        if (command.contains(":")) command = command.split(":")[1];

        if (!Storage.isCommandBlocked(commandLowerCased)) return;

        if (PermissionUtil.hasBypassPermission(player, command)) return;

        player.sendMessage(Storage.CANCEL_COMMANDS_MESSAGE.replace("%command%", rawCommand.replaceFirst("/", "")));

        final String alertMessage = Storage.NOTIFY_ALERT.replace("%player%", player.getName()).replace("%command%", command);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            Player notifier = Bukkit.getServer().getPlayer(uuid);
            notifier.sendMessage(alertMessage);
        });
        if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
        event.setCancelled(true);
    }
}
