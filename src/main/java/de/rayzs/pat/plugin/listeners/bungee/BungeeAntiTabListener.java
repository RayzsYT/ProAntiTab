package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeAntiTabListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (PermissionUtil.hasBypassPermission(player)) return;
        if (Storage.Blacklist.isBlocked(player, event.getCursor(), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getServer().getInfo().getName()))
            event.getSuggestions().clear();
    }

    @EventHandler
    public void onTabComplete(TabCompleteResponseEvent event) {
        if (!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (PermissionUtil.hasBypassPermission(player)) return;

        event.getSuggestions().removeIf(command -> Storage.Blacklist.isBlocked(player, command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getServer().getInfo().getName()));

        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.call(player, event.getSuggestions());
        if (filteredSuggestionEvent.isCancelled()) event.getSuggestions().clear();
    }
}
