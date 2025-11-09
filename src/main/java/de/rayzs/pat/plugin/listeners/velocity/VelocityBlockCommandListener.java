package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
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

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        handleCommand(event);
    }

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
        String command = StringUtils.getFirstArg(event.getCommand());

        String serverName = player.getCurrentServer().isPresent()
                ? player.getCurrentServer().get().getServerInfo().getName()
                : "unknown";

        if (PermissionUtil.hasBypassPermission(player, command) || Storage.Blacklist.isDisabledServer(serverName))
            return event;

        final String displayCommand = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");


        List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getUsername(),
                "%command%", displayCommand,
                "%server%", serverName
        );

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {

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

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED)
            return event;

        if (!Storage.Blacklist.canPlayerAccessChat(player, command, serverName)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(
                    player,
                    event.getCommand(),
                    true,
                    !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);

            if (executeCommandEvent.isBlocked()) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());

                if (!executeCommandEvent.doesNotify())
                    return event;

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

        ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), false, false);
        if (executeCommandEvent.isBlocked())
            event.setResult(CommandExecuteEvent.CommandResult.denied());

        return event;
    }
}