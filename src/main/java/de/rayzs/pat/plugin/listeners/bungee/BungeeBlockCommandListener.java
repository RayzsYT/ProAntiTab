package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.*;

public class BungeeBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onChat(ChatEvent event) {
        if(!Storage.CANCEL_COMMANDS) return;

        Connection connection = event.getSender();
        if(!(connection instanceof ProxiedPlayer) || !event.getMessage().startsWith("/")) return;

        ProxiedPlayer player = (ProxiedPlayer) connection;
        String rawCommand = event.getMessage(), command = rawCommand.replaceFirst("/", "").toLowerCase();

        if(rawCommand.equals("/")) return;
        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo != null ? serverInfo.getName() : "unknown",
                alertMessage = Storage.NOTIFY_ALERT.replace("%player%", player.getName()).replace("%command%", command).replace("%server%", serverName);


        if(Storage.isPluginsCommand(command) && Storage.USE_CUSTOM_PLUGINS && !PermissionUtil.hasBypassPermission(player, command)) {
            for (String line : Storage.CUSTOM_PLUGINS) MessageTranslator.send(player, line.replace("%command%", rawCommand.replaceFirst("/", "")));
            event.setCancelled(true);

            if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null).forEach(uuid -> {
                ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
                MessageTranslator.send(target, alertMessage);
            });
            return;
        }

        if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
            if(Storage.isCommandBlockedPrecise(command)) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;

            for (String line : Storage.CANCEL_COMMANDS_MESSAGE) MessageTranslator.send(player, line.replace("%command%", rawCommand.replaceFirst("/", "")));
            event.setCancelled(true);
            return;
        }

        if (command.contains(":")) {
            String[] commandSplit = command.split(":");
            if(commandSplit.length > 1) command = commandSplit[1];
        }

        if(!Storage.isCommandBlocked(command) || PermissionUtil.hasBypassPermission(player, command)) return;
        for (String line : Storage.CANCEL_COMMANDS_MESSAGE) MessageTranslator.send(player, line.replace("%command%", rawCommand.replaceFirst("/", "")));

        if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> ProxyServer.getInstance().getPlayer(uuid) != null).forEach(uuid -> {
            ProxiedPlayer target = ProxyServer.getInstance().getPlayer(uuid);
            MessageTranslator.send(target, alertMessage);
        });
        event.setCancelled(true);
    }
}
