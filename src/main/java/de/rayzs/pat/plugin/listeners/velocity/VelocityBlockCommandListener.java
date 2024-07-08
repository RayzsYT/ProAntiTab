package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.command.CommandSource;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import java.util.List;

public class VelocityBlockCommandListener {

    private final ProxyServer server;

    public VelocityBlockCommandListener(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent event) {
        if(!event.getResult().isAllowed()) return;
        CommandSource commandSource = event.getCommandSource();

        if(!(commandSource instanceof Player)) return;

        Player player = (Player) commandSource;
        String command = event.getCommand(),
                serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown";

        command = StringUtils.replaceFirst(command, "/", "");
        command = StringUtils.getFirstArg(command);
        command = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");

        if(PermissionUtil.hasBypassPermission(player, command, false)) return;

        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getUsername(), "%command%", command, "%server%", serverName);

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));

            return;
        }

        if(Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command)) {
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_VERSION.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));

            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED) return;
        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", command.replaceFirst("/", ""));

        boolean listed, serverListed, ignored;

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.doesGroupBypass(player, command, false, true, false, player.getCurrentServer().get().getServerInfo().getName())) return;

            listed = Storage.Blacklist.isListed(command, false, true, false);
            serverListed = Storage.Blacklist.isListed(player, command, false, listed, false, serverName);
            ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

            if(ignored ? !listed && serverListed : serverListed) return;

            event.setResult(CommandExecuteEvent.CommandResult.denied());
            MessageTranslator.send(player, cancelCommandMessage);
            return;
        }

        if(Storage.Blacklist.doesGroupBypass(player, command, true, true, false, player.getCurrentServer().get().getServerInfo().getName())) return;

        listed = Storage.Blacklist.isListed(command, true, true, false);
        serverListed = Storage.Blacklist.isListed(player, command, true, listed, false, serverName);
        ignored = Storage.Blacklist.isOnIgnoredServer(serverName);

        if(!listed && !serverListed || listed && serverListed && ignored) return;

        event.setResult(CommandExecuteEvent.CommandResult.denied());
        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) MessageTranslator.send(server.getConsoleCommandSource(), notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent()).forEach(uuid -> MessageTranslator.send(server.getPlayer(uuid).get(), notificationMessage));
    }
}
