package de.rayzs.pat.plugin.listeners;

import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.*;

import java.util.*;

public class BungeeAntiTabListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onTabComplete(TabCompleteEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if(PermissionUtil.hasBypassPermission(player)) return;

        for (int i = 0; i < event.getSuggestions().size(); i++) {
            String command = (String) event.getSuggestions().toArray()[i], tempName = command;

            if (tempName.contains(":")) tempName = tempName.split(":")[1];
            if (Storage.isCommandBlocked(tempName)) {
                if(PermissionUtil.hasBypassPermission(player, tempName)) event.getSuggestions().add(command);
                else event.getSuggestions().remove(command);
            }
        }

        String command = event.getCursor();
        if(command.contains(" ")) command = command.split(" ")[0].toLowerCase();
        if (command.contains(":")) command = command.split(":")[1];
        if(command.startsWith("/")) command = command.replaceFirst("/", "");

        if (!Storage.isCommandBlocked(command)) return;
        event.setCancelled(!PermissionUtil.hasBypassPermission(player, command));
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onTabComplete(TabCompleteResponseEvent event) {
        if(!(event.getReceiver() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
        if(PermissionUtil.hasBypassPermission(player)) return;

        int size = event.getSuggestions().size();
        List<String> dummy = new ArrayList<>(event.getSuggestions());

        for (int i = 0; i < size; i++) {
            String command = dummy.get(i), tempName = command;

            if (tempName.startsWith("/")) tempName = tempName.replaceFirst("/", "");

            if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
                if(Storage.isCommandBlockedPrecise(tempName) && !event.getSuggestions().contains(command))
                    event.getSuggestions().add(command);
                else if(!Storage.isCommandBlockedPrecise(tempName) && !PermissionUtil.hasPermissionWithResponse(player, tempName))
                    event.getSuggestions().remove(command);
                continue;
            }

            if (tempName.contains(":")) tempName = tempName.split(":")[1];

            if (!Storage.isCommandBlocked(tempName)) continue;
            if(PermissionUtil.hasBypassPermission(player, tempName)) continue;
            event.getSuggestions().remove(command);
        }
    }
}
