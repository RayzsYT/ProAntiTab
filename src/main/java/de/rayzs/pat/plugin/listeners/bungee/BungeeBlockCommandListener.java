package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.event.events.ExecuteCommandEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.connection.*;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.event.*;
import net.md_5.bungee.event.*;

import java.util.List;

public class BungeeBlockCommandListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {
        if (event.isCancelled()) return;

        Connection connection = event.getSender();
        if (! (connection instanceof ProxiedPlayer) || !event.getMessage().startsWith("/"))
            return;

        ProxiedPlayer player = (ProxiedPlayer) connection;
        String serverName = player.getServer().getInfo().getName();
        String command = event.getMessage();

        boolean bypassPermission = PermissionUtil.hasBypassPermission(player, command);

        if (bypassPermission)
            return;

        command = command.startsWith("/") ? command.substring(1) : command;
        command = StringUtils.getFirstArg(command);

        final String displayCommand = StringUtils.replaceTriggers(command, "", "\\", "<", ">", "&");

        //command = command.toLowerCase();

        List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getName(),
                "%command%", displayCommand,
                "%server%", serverName);

        if (!Storage.ConfigSections.Settings.CANCEL_COMMAND.ENABLED)
            return;

        if (!Storage.Blacklist.canPlayerAccessChat(player, command, serverName)) {
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

                Storage.NOTIFY_PLAYERS.forEach(uuid -> {
                    Object p = Storage.getLoader().getPlayerObjByUUID(uuid);

                    if (p != null) {
                        MessageTranslator.send(p, notificationMessage);
                    }
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
