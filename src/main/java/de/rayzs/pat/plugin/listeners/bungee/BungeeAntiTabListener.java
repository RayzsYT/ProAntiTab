package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.*;

public class BungeeAntiTabListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if(PermissionUtil.hasBypassPermission(player)) return;

        event.getSuggestions().removeIf(command -> {
            if(Storage.TURN_BLACKLIST_TO_WHITELIST)
                return !Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
            else return Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
        });
    }

    @EventHandler
    public void onTabComplete(TabCompleteResponseEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if(PermissionUtil.hasBypassPermission(player)) return;

        event.getSuggestions().removeIf(command -> {
            if(Storage.TURN_BLACKLIST_TO_WHITELIST)
                return !Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
            else return Storage.isCommandBlocked(command) && !PermissionUtil.hasBypassPermission(player, command);
        });
    }
}
