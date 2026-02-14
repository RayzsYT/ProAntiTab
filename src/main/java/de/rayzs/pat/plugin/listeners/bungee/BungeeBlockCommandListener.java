package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.connection.*;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.event.*;
import net.md_5.bungee.event.*;

import java.util.List;

public class BungeeBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        if (event.isCancelled()) return;

        final Connection connection = event.getSender();
        if (! (connection instanceof ProxiedPlayer) || !event.getMessage().startsWith("/"))
            return;

        final ProxiedPlayer player = (ProxiedPlayer) connection;
        final CommandSender sender = CommandSenderHandler.from(player);
        final String serverName = player.getServer().getInfo().getName();

        String command = event.getMessage();

        if (PermissionUtil.hasBypassPermission(sender, command) || Storage.Blacklist.isDisabledServer(serverName))
            return;

        command = command.startsWith("/") ? command.substring(1) : command;
        command = StringUtils.getFirstArg(command);

        if (Communicator.get().hasConnectedClients() && Storage.ConfigSections.Settings.AUTO_LOWERCASE_COMMANDS.isCommand(command)) {
            command = command.toLowerCase();
        }

        final String displayCommand = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");


        final List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getName(),
                "%command%", displayCommand,
                "%server%", serverName);

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {

            Communicator.Proxy2Backend.sendNotification(player.getUniqueId(), serverName, displayCommand);

            MessageTranslator.send(
                    player,
                    Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE,
                    "%command%", StringUtils.getFirstArg(displayCommand)
            );

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                Logger.info(notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });

            event.setCancelled(true);
            return;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {

            Communicator.Proxy2Backend.sendNotification(player.getUniqueId(), serverName, displayCommand);

            MessageTranslator.send(
                    player,
                    Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE,
                    "%command%", StringUtils.getFirstArg(displayCommand)
            );

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                Logger.info(notificationMessage);

            Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });

            event.setCancelled(true);
            return;
        }

        final List<Group> groups = GroupManager.getPlayerGroups(sender);
        final boolean cancelBlockedCommand = Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED;

        boolean allowed = Storage.Blacklist.canPlayerAccessChat(sender, groups, command, serverName);
        boolean blockedNamespace = false;

        if (!Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(sender)) {
            blockedNamespace = cancelBlockedCommand
                    ? Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command)
                    : Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesAlwaysBlock(command);

            if (blockedNamespace) allowed = false;
        }

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED && !blockedNamespace) {
            return;
        }

        if (!allowed) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(
                    player,
                    event.getMessage(),
                    true,
                    !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED
            );

            if (executeCommandEvent.isBlocked()) {
                event.setCancelled(true);

                if (!executeCommandEvent.doesNotify())
                    return;

                Communicator.Proxy2Backend.sendNotification(player.getUniqueId(), serverName, displayCommand);

                if (Storage.SEND_CONSOLE_NOTIFICATION)
                    Logger.info(notificationMessage);

                Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                    Object p = Storage.getLoader().getPlayerObjByUUID(uuid);

                    if (p != null) {
                        MessageTranslator.send(p, notificationMessage);
                    }
                });

            }

            if (executeCommandEvent.isCancelled())
                return;
        }

        ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getMessage(), false, false);
        if (executeCommandEvent.isBlocked())
            event.setCancelled(true);
    }
}
