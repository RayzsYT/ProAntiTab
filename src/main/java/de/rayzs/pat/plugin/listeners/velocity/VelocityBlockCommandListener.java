package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.MessageTranslator;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;
import net.kyori.adventure.text.minimessage.MiniMessage;

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
        String command = event.getCommand();

        if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
            if(Storage.isCommandBlockedPrecise(command)) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;

            player.sendMessage(MiniMessage.miniMessage().deserialize(MessageTranslator.translate(Storage.CANCEL_COMMANDS_MESSAGE.replace("%command%", command.replaceFirst("/", "")))));
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        if (command.contains(":")) {
            String[] commandSplit = command.split(":");
            if(commandSplit.length > 1) command = commandSplit[1];
        }

        if(!Storage.isCommandBlocked(command) || PermissionUtil.hasBypassPermission(player, command)) return;
        player.sendMessage(MiniMessage.miniMessage().deserialize(MessageTranslator.translate(Storage.CANCEL_COMMANDS_MESSAGE.replace("%command%", command.replaceFirst("/", "")))));

        final String serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown",
                alertMessage = Storage.NOTIFY_ALERT.replace("%player%", player.getUsername()).replace("%command%", command).replace("%server%", serverName);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> server.getPlayer(uuid).isPresent() && server.getPlayer(uuid).get().isActive()).forEach(uuid -> {
            server.getPlayer(uuid).get().sendMessage(MiniMessage.miniMessage().deserialize(MessageTranslator.translate(alertMessage)));
        });

        if(Storage.CONSOLE_NOTIFICATION_ENABLED) server.getConsoleCommandSource().sendMessage(MiniMessage.miniMessage().deserialize(MessageTranslator.translate(alertMessage)));
        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }
}
