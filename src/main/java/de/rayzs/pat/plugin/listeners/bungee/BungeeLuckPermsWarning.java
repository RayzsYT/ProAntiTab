package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.LuckPermsWarning;
import de.rayzs.pat.utils.sender.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;

public class BungeeLuckPermsWarning implements Listener {

    @EventHandler
    public void onPlayerCommandExecution(final ChatEvent event) {
        if (LuckPermsWarning.alreadyAnnounced()) return;

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_WORLD.ENABLED || Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED) {
            return;
        }


        if (! (event.getSender() instanceof ProxiedPlayer player)) return;

        final CommandSender sender = CommandSender.from(player);
        final String command = event.getMessage();

        if (LuckPermsWarning.shouldSendLuckPermsWarning(sender, command.substring(1))) {
            ProxyServer.getInstance().getScheduler().schedule(
                    BungeeLoader.getPlugin(),
                    () -> LuckPermsWarning.sendAnnouncement(sender),
                    1, TimeUnit.SECONDS
            );
        }
    }
}
