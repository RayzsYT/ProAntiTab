package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;

public class VelocityBlockCommandListener {

    private final ProxyServer server;

    public VelocityBlockCommandListener(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onPlayerChat(CommandExecuteEvent event) {
        if(!Storage.CANCEL_COMMANDS || !event.getResult().isAllowed()) return;

        CommandSource commandSource = event.getCommandSource();

        if(!(commandSource instanceof Player)) return;

        Player player = (Player) commandSource;
        String command = event.getCommand(),
                serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown",
                alertMessage = Storage.NOTIFY_ALERT.replace("%player%", player.getUsername()).replace("%command%", command).replace("%server%", serverName);

        if(Storage.isPluginsCommand(command) && Storage.USE_CUSTOM_PLUGINS && !PermissionUtil.hasBypassPermission(player, command)) {
            for (String line : Storage.CUSTOM_PLUGINS) MessageTranslator.send(player, line.replace("%command%", command.replaceFirst("/", "")));
            event.setResult(CommandExecuteEvent.CommandResult.denied());

            if(Storage.CONSOLE_NOTIFICATION_ENABLED) Logger.info(alertMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent() && server.getPlayer(uuid).get().isActive()).forEach(uuid -> {
                MessageTranslator.send(server.getPlayer(uuid).get(), alertMessage);
            });
            return;
        }

        if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
            if(Storage.isBlocked(command, true)) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;

            for (String line : Storage.CANCEL_COMMANDS_MESSAGE) MessageTranslator.send(player, line.replace("%command%", command.replaceFirst("/", "")));
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        if(!Storage.isBlocked(command, false) || PermissionUtil.hasBypassPermission(player, command)) return;
        for (String line : Storage.CANCEL_COMMANDS_MESSAGE) MessageTranslator.send(player, line.replace("%command%", command.replaceFirst("/", "")));

        if(Storage.CONSOLE_NOTIFICATION_ENABLED) MessageTranslator.send(server.getConsoleCommandSource(), alertMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent() && server.getPlayer(uuid).get().isActive()).forEach(uuid -> {
            MessageTranslator.send(server.getPlayer(uuid).get(), alertMessage);
        });

        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }
}
