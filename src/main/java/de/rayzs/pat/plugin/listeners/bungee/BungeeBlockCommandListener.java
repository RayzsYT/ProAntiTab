package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.ProxyServer;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.event.*;
import net.md_5.bungee.event.*;
import java.util.List;

public class BungeeBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        if(event.isCancelled()) return;

        Connection connection = event.getSender();
        if(!(connection instanceof ProxiedPlayer) || !event.getMessage().startsWith("/")) return;

        ProxiedPlayer player = (ProxiedPlayer) connection;

        String rawCommand = event.getMessage(), command = rawCommand.toLowerCase();

        command = StringUtils.replaceFirst(command, "/", "");
        command = StringUtils.getFirstArg(command);
        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");

        if(PermissionUtil.hasBypassPermission(player, command, false)) return;

        if(rawCommand.equals("/")) return;
        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo != null ? serverInfo.getName() : "unknown";
        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getName(), "%command%", command, "%server%", serverName);

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            if(!PATEventManager.useDefaultActions(player, command, PATEvent.Situation.EXECUTED_PLUGINS_COMMAND)) return;

            event.setCancelled(true);
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", rawCommand.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
                if(proxiedPlayer != null) MessageTranslator.send(proxiedPlayer, notificationMessage);
            });

            return;
        }

        if(Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            if(!PATEventManager.useDefaultActions(player, command, PATEvent.Situation.EXECUTED_VERSION_COMMAND)) return;

            event.setCancelled(true);
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE, "%command%", rawCommand.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
                if(proxiedPlayer != null) MessageTranslator.send(proxiedPlayer, notificationMessage);
            });

            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED) return;

        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", command);

        boolean listed, serverListed, ignored;
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.doesGroupBypass(player, command, true, true, false, serverName)) return;
            listed = Storage.Blacklist.isListed(command, false, true, false);
            serverListed = Storage.Blacklist.isListed(player, command, false, listed, false, serverName);
            ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

            if(ignored ? !listed && serverListed : serverListed) return;
            if(!PATEventManager.useDefaultActions(player, command, PATEvent.Situation.EXECUTED_BLOCKED_COMMAND)) return;

            event.setCancelled(true);
            MessageTranslator.send(player, cancelCommandMessage);
            return;
        }

        if(Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command) && !Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player)) {
            if(!PATEventManager.useDefaultActions(player, command, PATEvent.Situation.EXECUTED_BLOCKED_COMMAND)) return;

            event.setCancelled(true);
            MessageTranslator.send(player, cancelCommandMessage);

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
                if(proxiedPlayer != null) MessageTranslator.send(proxiedPlayer, notificationMessage);
            });

            return;
        }

        if(Storage.Blacklist.doesGroupBypass(player, command, true, true, false, serverName)) return;

        listed = Storage.Blacklist.isListed(command, true, true, false);
        serverListed = Storage.Blacklist.isListed(player, command, true, listed, false, serverName);
        ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

        if(!listed && !serverListed || listed && serverListed && ignored) return;
        if(!PATEventManager.useDefaultActions(player, command, PATEvent.Situation.EXECUTED_BLOCKED_COMMAND)) return;

        event.setCancelled(true);
        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
        Storage.NOTIFY_PLAYERS.forEach(uuid -> {
            ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
            if(proxiedPlayer != null) MessageTranslator.send(proxiedPlayer, notificationMessage);
        });
    }
}
