package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.plugin.VelocityLoader;
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

    private final ProxyServer server;

    public VelocityBlockCommandListener(ProxyServer server) {
        this.server = server;
    }

    @Subscribe (order = PostOrder.EARLY)
    public void onCommandExecute(CommandExecuteEvent event) {
        if(!event.getResult().isAllowed()) return;
        CommandSource commandSource = event.getCommandSource();

        if(!(commandSource instanceof Player)) return;

        Player player = (Player) commandSource;
        String rawCommand = event.getCommand(), command = rawCommand,
                serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown";

        command = StringUtils.replaceFirst(command, "/", "");
        command = StringUtils.getFirstArg(command);
        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");

        if(PermissionUtil.hasBypassPermission(player, command, false)) return;

        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getUsername(), "%command%", command, "%server%", serverName);

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, true);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            if(executeCommandEvent.isCancelled()) return;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));

            return;
        }

        if(Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, true);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            if(executeCommandEvent.isCancelled()) return;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));

            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED) return;
        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE, "%command%", command.replaceFirst("/", ""));

        boolean listed, serverListed, ignored;

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.doesGroupBypass(player, command, false, true, false, player.getCurrentServer().get().getServerInfo().getName())) {
                ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, false);
                if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
                return;
            }

            listed = Storage.Blacklist.isListed(command, false, true, false);
            serverListed = Storage.Blacklist.isListed(player, command, false, listed, false, serverName);
            ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

            if(ignored ? !listed && serverListed : serverListed) {
                ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, false);
                if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
                return;
            }

            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, true);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            if(executeCommandEvent.isCancelled()) return;

            MessageTranslator.send(player, cancelCommandMessage);
            return;
        }

        if(Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command) && !Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, true);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            if(executeCommandEvent.isCancelled()) return;

            MessageTranslator.send(player, cancelCommandMessage);

            if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));

            return;
        }

        if(Storage.Blacklist.doesGroupBypass(player, command, true, true, false, player.getCurrentServer().get().getServerInfo().getName())) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, false);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        listed = Storage.Blacklist.isListed(command, true, true, false);
        serverListed = Storage.Blacklist.isListed(player, command, true, listed, false, serverName);
        ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

        if(!listed && !serverListed || listed && serverListed && ignored) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, false);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        if(Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command) && Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player.getUniqueId())) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, false);
            if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, rawCommand, true);
        if(executeCommandEvent.isBlocked()) event.setResult(CommandExecuteEvent.CommandResult.denied());
        if(executeCommandEvent.isCancelled()) return;

        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));
    }

    public static boolean handleCommand(Player player, String commandSource) {
        String command = commandSource,
                serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown";

        boolean blocked = false;

        command = StringUtils.replaceFirst(command, "/", "");
        command = StringUtils.getFirstArg(command);
        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");

        if (PermissionUtil.hasBypassPermission(player, command, false)) return true;

        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getUsername(), "%command%", command, "%server%", serverName);

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, true);
            if (executeCommandEvent.isBlocked()) blocked = true;
            if (executeCommandEvent.isCancelled()) return true;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(VelocityLoader.getServer().getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> VelocityLoader.getServer().getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(VelocityLoader.getServer().getPlayer(uuid).get(), notificationMessage));

            return !blocked;
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, true);
            if (executeCommandEvent.isBlocked()) blocked = true;
            if (executeCommandEvent.isCancelled()) return true;

            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(VelocityLoader.getServer().getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> VelocityLoader.getServer().getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(VelocityLoader.getServer().getPlayer(uuid).get(), notificationMessage));

            return !blocked;
        }

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED) return true;
        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", command.replaceFirst("/", ""));

        boolean listed, serverListed, ignored;

        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if (Storage.Blacklist.doesGroupBypass(player, command, false, true, false, player.getCurrentServer().get().getServerInfo().getName())) {
                ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, false);
                return !executeCommandEvent.isBlocked();
            }

            listed = Storage.Blacklist.isListed(command, false, true, false);
            serverListed = Storage.Blacklist.isListed(player, command, false, listed, false, serverName, true);
            ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

            if (ignored ? !listed && serverListed : serverListed) {
                ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, false);
                return !executeCommandEvent.isBlocked();
            }

            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, true);
            if (executeCommandEvent.isBlocked()) blocked = true;
            if (executeCommandEvent.isCancelled()) return true;

            MessageTranslator.send(player, cancelCommandMessage);
            return !blocked;
        }

        if (Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command) && !Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player)) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, true);
            if (executeCommandEvent.isBlocked()) blocked = true;
            if (executeCommandEvent.isCancelled()) return true;

            MessageTranslator.send(player, cancelCommandMessage);

            if (Storage.SEND_CONSOLE_NOTIFICATION)
                MessageTranslator.send(VelocityLoader.getServer().getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> VelocityLoader.getServer().getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(VelocityLoader.getServer().getPlayer(uuid).get(), notificationMessage));

            return !blocked;
        }

        if (Storage.Blacklist.doesGroupBypass(player, command, true, true, false, player.getCurrentServer().get().getServerInfo().getName())) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, false);
            return !executeCommandEvent.isBlocked();
        }

        listed = Storage.Blacklist.isListed(command, true, true, false);
        serverListed = Storage.Blacklist.isListed(player, command, true, listed, false, serverName);
        ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

        if (!listed && !serverListed || listed && serverListed && ignored) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, false);
            return !executeCommandEvent.isBlocked();
        }

        if (Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command) && Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(player.getUniqueId())) {
            ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, false);
            return !executeCommandEvent.isBlocked();
        }

        ExecuteCommandEvent executeCommandEvent = PATEventHandler.callExecuteCommandEvents(player, commandSource, true);
        if (executeCommandEvent.isBlocked()) blocked = true;
        if (executeCommandEvent.isCancelled()) return true;

        MessageTranslator.send(player, cancelCommandMessage);

        if (Storage.SEND_CONSOLE_NOTIFICATION)
            MessageTranslator.send(VelocityLoader.getServer().getConsoleCommandSource(), notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> VelocityLoader.getServer().getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(VelocityLoader.getServer().getPlayer(uuid).get(), notificationMessage));
        return !blocked;
    }
}