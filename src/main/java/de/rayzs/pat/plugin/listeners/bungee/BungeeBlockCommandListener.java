package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.List;

public class BungeeBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        if(event.isCancelled()) return;

        Connection connection = event.getSender();
        if(!(connection instanceof ProxiedPlayer) || !event.getMessage().startsWith("/")) return;

        ProxiedPlayer player = (ProxiedPlayer) connection;

        String rawCommand = event.getMessage(), command = rawCommand.replaceFirst("/", "").toLowerCase();

        if(command.contains(" ")) {
            String[] split = command.split(" ");
            if(split.length > 0) command = split[0];
        }

        if(command.contains("\n"))
            command = command.replace("\n", "\\n");

        if(rawCommand.equals("/")) return;
        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo != null ? serverInfo.getName() : "unknown";
        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getName(), "%command%", command, "%server%", serverName);


        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isPluginsCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            event.setCancelled(true);
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", rawCommand.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                Player target = Bukkit.getServer().getPlayer(uuid);
                MessageTranslator.send(target, notificationMessage);
            });
            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED) return;

        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", rawCommand.replaceFirst("/", ""));
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.doesGroupBypass(player, command, true, player.getServer().getInfo().getName())) return;
            if(Storage.Blacklist.isListed(player, command, true, player.getServer().getInfo().getName())) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;
            event.setCancelled(true);
            MessageTranslator.send(player, cancelCommandMessage);
            return;
        }

        if (!Storage.Blacklist.isBlocked(player, command, player.getServer().getInfo().getName())) return;
        if (PermissionUtil.hasBypassPermission(player, command)) return;
        event.setCancelled(true);
        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            Player target = Bukkit.getServer().getPlayer(uuid);
            MessageTranslator.send(target, notificationMessage);
        });
    }
}
