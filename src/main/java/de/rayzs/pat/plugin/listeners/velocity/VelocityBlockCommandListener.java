package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.event.*;
import java.util.List;

public class VelocityBlockCommandListener {

    public static CommandExecuteEvent handleCommand(Player player, String command) {
        return handleCommand(new CommandExecuteEvent(player, command));
    }

    public static CommandExecuteEvent handleCommand(CommandExecuteEvent event) {
        if(!event.getResult().isAllowed())
            return event;

        CommandSource commandSource = event.getCommandSource();
        Object consoleSender = Storage.getLoader().getConsoleSender();

        if(! (commandSource instanceof Player))
            return event;

        Player player = (Player) commandSource;
        String command = event.getCommand();

        String serverName = player.getCurrentServer().isPresent()
                ? player.getCurrentServer().get().getServerInfo().getName()
                : "unknown";

        boolean bypassPermission = PermissionUtil.hasBypassPermission(player, command);

        if (bypassPermission)
            return event;

        command = StringUtils.getFirstArg(command);

        final String displayCommand = command;

        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");
        command = command.toLowerCase();


        List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getUsername(),
                "%command%", displayCommand,
                "%server%", serverName
        );

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), true);
            if (executeCommandEvent.isBlocked())
                event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (executeCommandEvent.isCancelled())
                return event;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE,  "%command%", displayCommand);

            if(Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(consoleSender, notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });

            return event;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), true);
            if (executeCommandEvent.isBlocked())
                event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (executeCommandEvent.isCancelled())
                return event;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE,  "%command%", displayCommand);

            if(Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(consoleSender, notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });

            return event;
        }

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED)
            return event;

        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE, "%command%", displayCommand);

        if (!Storage.Blacklist.canPlayerAccessChat(player, command, serverName)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), true);

            if (executeCommandEvent.isBlocked())
                event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (executeCommandEvent.isCancelled())
                return event;

            MessageTranslator.send(player, cancelCommandMessage);

            if(Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(consoleSender, notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().forEach(uuid -> {
                Object p = Storage.getLoader().getPlayerObjByUUID(uuid);
                if (p != null) {
                    MessageTranslator.send(p, notificationMessage);
                }
            });
        }

        ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), false);
        if (executeCommandEvent.isBlocked())
            event.setResult(CommandExecuteEvent.CommandResult.denied());

        return event;
    }
}