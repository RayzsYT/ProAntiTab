package de.rayzs.pat.plugin.listeners.bungee;

import com.google.gson.JsonPrimitive;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.HashMap;

public class BungeeAntiTabListener implements Listener {

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {

        // Ignoring this at first, due to transfer to the packet-based solution only. Hehe

        /*
        if(!(event.getSender() instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo.getName();

        if (Storage.Blacklist.isDisabledServer(serverName))
            return;

        if (PermissionUtil.hasBypassPermission(player))
            return;

        if (!Storage.Blacklist.canPlayerAccessTab(player, event.getCursor(), serverName)) {
            event.getSuggestions().clear();
            return;
        }

        event.getSuggestions().removeIf(suggestion -> !Storage.Blacklist.canPlayerAccessTab(player, suggestion, serverName));
         */
    }

    @EventHandler
    public void onTabComplete(TabCompleteResponseEvent event) {

        // Ignoring this at first, due to transfer to the packet-based solution only. Hehe

        /*
        if (!(event.getSender() instanceof ProxiedPlayer))
            return;

        ProxiedPlayer player = (ProxiedPlayer) event.getSender();

        ServerInfo serverInfo = player.getServer().getInfo();
        String serverName = serverInfo.getName();

        if (Storage.Blacklist.isDisabledServer(serverName))
            return;

        if(PermissionUtil.hasBypassPermission(player)) 
            return;

        event.getSuggestions().removeIf(command -> !Storage.Blacklist.canPlayerAccessTab(player, command, serverName));
        
        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.callFilteredSuggestionEvents(player, event.getSuggestions());
        if (filteredSuggestionEvent.isCancelled()) event.getSuggestions().clear();
         */
    }
}
