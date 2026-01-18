package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.event.*;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;

import java.util.List;

public class VelocityBlockCommandListener {

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        handleCommand(event);
    }

    public static CommandExecuteEvent handleCommand(Player player, String command) {
        return handleCommand(new CommandExecuteEvent(player, command));
    }

    public static CommandExecuteEvent handleCommand(CommandExecuteEvent event) {
        if (!event.getResult().isAllowed()) {
            return event;
        }

        final CommandSource commandSource = event.getCommandSource();
        final Object consoleSender = Storage.getLoader().getConsoleSender();

        if(! (commandSource instanceof Player))
            return event;

        final Player player = (Player) commandSource;
        final CommandSender sender = CommandSenderHandler.from(player);
        final String serverName = player.getCurrentServer().isPresent()
                ? player.getCurrentServer().get().getServerInfo().getName()
                : "unknown";

        final String command = StringUtils.getFirstArg(event.getCommand());

        if (PermissionUtil.hasBypassPermission(sender, command) || Storage.Blacklist.isDisabledServer(serverName))
            return event;

        final String displayCommand = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");


        final List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getUsername(),
                "%command%", displayCommand,
                "%server%", serverName
        );

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {

            Communicator.sendNotificationPacket(sender, displayCommand);

            MessageTranslator.send(
                    player,
                    Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE,
                    "%command%", StringUtils.getFirstArg(displayCommand)
            );

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(consoleSender, notificationMessage);

            Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });

            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return event;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {

            Communicator.sendNotificationPacket(sender, displayCommand);

            MessageTranslator.send(
                    player,
                    Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE,
                    "%command%", StringUtils.getFirstArg(displayCommand)
            );

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(consoleSender, notificationMessage);

            Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });

            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return event;
        }

        final boolean cancelBlockedCommand = Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED;
        final List<Group> groups = GroupManager.getPlayerGroups(sender);

        boolean allowed = Storage.Blacklist.canPlayerAccessChat(sender, groups, command, serverName);
        boolean blockedNamespace = false;


        if (!Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(sender)) {
            blockedNamespace = cancelBlockedCommand
                    ? Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command)
                    : Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesAlwaysBlock(command);

            if (blockedNamespace) allowed = false;
        }


        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED && !blockedNamespace) {
            return event;
        }


        if (!allowed) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(
                    player,
                    event.getCommand(),
                    true,
                    !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);

            if (executeCommandEvent.isBlocked()) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());

                if (!executeCommandEvent.doesNotify())
                    return event;

                Communicator.sendNotificationPacket(sender, displayCommand);

                if (Storage.SEND_CONSOLE_NOTIFICATION)
                    MessageTranslator.send(consoleSender, notificationMessage);

                Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                    Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                    if (p != null) {
                        MessageTranslator.send(p, notificationMessage);
                    }
                });
            }

            if (executeCommandEvent.isCancelled())
                return event;
        }


        final ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), false, false);

        if (executeCommandEvent.isBlocked()) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
        }

        return event;
    }
}