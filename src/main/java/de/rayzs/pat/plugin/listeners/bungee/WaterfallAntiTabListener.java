package de.rayzs.pat.plugin.listeners.bungee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.permission.PermissionUtil;
import io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class WaterfallAntiTabListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyDefineCommands(ProxyDefineCommandsEvent event) {

        System.out.println("CALL");

        if(!(event.getReceiver() instanceof ProxiedPlayer) || event.getCommands().isEmpty()) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
        String serverName = player.getServer().getInfo().getName();

        if (Storage.Blacklist.isDisabledServer(serverName))
            return;

        Map<String, CommandsCache> cache = Storage.getLoader().getCommandsCacheMap();

        if(!cache.containsKey(serverName))
            cache.put(serverName, new CommandsCache());

        CommandsCache commandsCache = cache.get(serverName);
        List<String> commandsAsString = new ArrayList<>();
        HashMap<String, Command> commandsMap = new HashMap<>(event.getCommands());

        event.getCommands().forEach((key, value) -> commandsAsString.add(key));
        commandsCache.handleCommands(commandsAsString, serverName);

        if (PermissionUtil.hasBypassPermission(player)) return;

        List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);
        event.getCommands().entrySet().removeIf(command -> {

            if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isCommand(command.getKey()) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isCommand(command.getKey())) {
                return false;
            }

            return playerCommands.contains(command.getKey());
        });

        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.callFilteredSuggestionEvents(player, new ArrayList<>(event.getCommands().keySet()));
        if (filteredSuggestionEvent.isCancelled()) event.getCommands().clear();

        for (String commandName : filteredSuggestionEvent.getSuggestions()) {

            if (!event.getCommands().containsKey(commandName)) 
                event.getCommands().put(commandName, commandsMap.get(commandName));
        
        }
    }
}
