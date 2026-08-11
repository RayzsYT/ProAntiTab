package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.utils.LuckPermsWarning;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class BukkitLuckPermsWarning implements Listener {

    @EventHandler
    public void onConsoleCommandExecution(final ServerCommandEvent event) {
        if (LuckPermsWarning.alreadyAnnounced()) return;

        final CommandSender sender = CommandSender.from(event.getSender());
        final String command = event.getCommand();

        if (LuckPermsWarning.shouldSendLuckPermsWarning(sender, command)) {
            PATScheduler.createScheduler(() -> {
                LuckPermsWarning.sendAnnouncement(sender);
            }, 20);
        }
    }

     @EventHandler
    public void onPlayerCommandExecution(final PlayerCommandPreprocessEvent event) {
        final CommandSender sender = CommandSender.from(event.getPlayer());
        final String command = event.getMessage();

         if (LuckPermsWarning.shouldSendLuckPermsWarning(sender, command.substring(1))) {
             PATScheduler.createScheduler(() -> {
                 LuckPermsWarning.sendAnnouncement(sender);
             }, 20);
         }
     }
}
