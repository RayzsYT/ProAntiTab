package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.PermissionUtil;
import org.bukkit.Bukkit;

import java.util.List;

public class VelocityBlockCommandListener {

    private final ProxyServer server;

    public VelocityBlockCommandListener(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onPlayerChat(CommandExecuteEvent event) {
        if(!event.getResult().isAllowed()) return;
        CommandSource commandSource = event.getCommandSource();

        if(!(commandSource instanceof Player)) return;

        Player player = (Player) commandSource;
        String command = event.getCommand(),
                serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown";
        List<String> notificationMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Messages.NOTIFICATION.ALERT, "%player%", player.getUsername(), "%command%", command, "%server%", serverName);

        if(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isPluginsCommand(command) && !PermissionUtil.hasBypassPermission(player, command)) {
            MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_PLUGIN.MESSAGE, "%command%", command.replaceFirst("/", ""));

            if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
            Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                org.bukkit.entity.Player target = Bukkit.getServer().getPlayer(uuid);
                MessageTranslator.send(target, notificationMessage);
            });
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        if(!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED) return;
        List<String> cancelCommandMessage = MessageTranslator.replaceMessageList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE, "%command%", command.replaceFirst("/", ""));

        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            if(Storage.Blacklist.isListed(command, true, player.getCurrentServer().get().getServerInfo().getName())) return;
            if(Storage.Blacklist.doesGroupBypass(player, command, true, player.getCurrentServer().get().getServerInfo().getName())) return;
            if(PermissionUtil.hasBypassPermission(player, command)) return;
            MessageTranslator.send(player, cancelCommandMessage);
            event.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }

        if (!Storage.Blacklist.isBlocked(player, command, player.getCurrentServer().get().getServerInfo().getName())) return;
        if (PermissionUtil.hasBypassPermission(player, command)) return;
        MessageTranslator.send(player, cancelCommandMessage);

        if(Storage.SEND_CONSOLE_NOTIFICATION) Logger.info(notificationMessage);
        Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
            org.bukkit.entity.Player target = Bukkit.getServer().getPlayer(uuid);
            MessageTranslator.send(target, notificationMessage);
        });

        event.setResult(CommandExecuteEvent.CommandResult.denied());
    }
}
