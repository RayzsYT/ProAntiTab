package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.api.storage.Storage;
import com.velocitypowered.api.event.*;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.event.*;
import java.util.List;

public class VelocityBlockCommandListener {

    private static ProxyServer SERVER;

    public VelocityBlockCommandListener(ProxyServer server) {
        SERVER = server;
    }

    public static CommandExecuteEvent handleCommand(Player player, String command) {
        return handleCommand(new CommandExecuteEvent(player, command));
    }

    public static CommandExecuteEvent handleCommand(CommandExecuteEvent event) {
        if(!event.getResult().isAllowed())
            return event;

        CommandSource commandSource = event.getCommandSource();

        if(! (commandSource instanceof Player))
            return event;

        Player player = (Player) commandSource;
        String command = event.getCommand();
        String serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown";

        boolean bypassPermission = PermissionUtil.hasBypassPermission(player, command);

        if (bypassPermission)
            return event;

        //command = command.replaceFirst("/", "");
        command = StringUtils.getFirstArg(command);
        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");
        command = command.toLowerCase();


        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getUsername(), "%command%", command, "%server%", serverName);

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), true);
            if (executeCommandEvent.isBlocked())
                event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (executeCommandEvent.isCancelled())
                return event;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE,  "%command%", command);

            if(Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(SERVER.getConsoleCommandSource(), notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().filter(uuid ->
                    SERVER.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(SERVER.getPlayer(uuid).get(), notificationMessage)
            );

            return event;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), true);
            if (executeCommandEvent.isBlocked())
                event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (executeCommandEvent.isCancelled())
                return event;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE,  "%command%", command);

            if(Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(SERVER.getConsoleCommandSource(), notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().filter(uuid ->
                    SERVER.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(SERVER.getPlayer(uuid).get(), notificationMessage)
            );

            return event;
        }

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED)
            return event;

        /*
        if (command.isEmpty())
            return event;
         */

        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE, "%command%", command);

        if (!Storage.Blacklist.canPlayerAccessChat(player, command, serverName)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), true);

            if (executeCommandEvent.isBlocked())
                event.setResult(CommandExecuteEvent.CommandResult.denied());

            if (executeCommandEvent.isCancelled())
                return event;

            MessageTranslator.send(player, cancelCommandMessage);

            if(Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(SERVER.getConsoleCommandSource(), notificationMessage);

            Storage.NOTIFY_PLAYERS.stream().filter(uuid ->
                    SERVER.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(SERVER.getPlayer(uuid).get(), notificationMessage)
            );
        }

        ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, event.getCommand(), false);
        if (executeCommandEvent.isBlocked())
            event.setResult(CommandExecuteEvent.CommandResult.denied());

        return event;
    }
}