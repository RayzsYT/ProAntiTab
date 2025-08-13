package de.rayzs.pat.plugin.listeners.bukkit;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;

public class BukkitBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onUnknownCommandRecognition(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        String rawCommand = StringUtils.getFirstArg(event.getMessage()),
                command =  rawCommand.substring(1);

        if (!Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED || event.isCancelled()) return;

        if (PermissionUtil.hasBypassPermission(player))
            return;

        if (Storage.getLoader().doesCommandExist(command))
            return;

        if (Bukkit.getHelpMap().getHelpTopic(rawCommand) != null)
            return;

        event.setCancelled(true);
        MessageTranslator.send(player, Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE, "%command%", command, "%world%", world.getName());
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandProcess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        final String commandFirstArg = StringUtils.getFirstArg(event.getMessage());

        if (Storage.ConfigSections.Settings.AUTO_LOWERCASE_COMMANDS.isCommand(commandFirstArg)) {
            String command = event.getMessage();

            if (command.contains(" ")) {
                String[] args = command.split(" ");
                args[0] = args[0].toLowerCase();
                command = String.join(" ", args);
            } else command = command.toLowerCase();

            if (!commandFirstArg.equals(command)) {
                player.chat(command);
                event.setCancelled(true);
                return;
            }
        }

        World world = player.getWorld();
        String command = event.getMessage(),
                worldName = world.getName();

        boolean bypassPermission = PermissionUtil.hasBypassPermission(player, command);

        command = command.substring(1);
        command = StringUtils.getFirstArg(command);

        final String displayName = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");
        command = command.toLowerCase();

        List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getName(),
                "%command%", displayName,
                "%world%", worldName);

        if (bypassPermission)
            return;

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED)
            return;
        
        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            return;

        if (!Storage.Blacklist.canPlayerAccessChat(player, command)) {
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

                if (Storage.SEND_CONSOLE_NOTIFICATION)
                    Logger.info(notificationMessage);

                Storage.NOTIFY_PLAYERS.stream().filter(uuid -> Bukkit.getServer().getPlayer(uuid) != null).forEach(uuid -> {
                    Player target = Bukkit.getServer().getPlayer(uuid);
                    MessageTranslator.send(target, notificationMessage);
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
