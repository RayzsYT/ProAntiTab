package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import de.rayzs.pat.utils.CommandsCache;
import net.md_5.bungee.event.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WaterfallAntiTabListener implements Listener {

    private static final HashMap<String, CommandsCache> COMMANDS_CACHE_MAP = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyDefineCommands(ProxyDefineCommandsEvent event) {
        if(!(event.getReceiver() instanceof ProxiedPlayer) || event.getCommands().isEmpty()) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
        String serverName = player.getServer().getInfo().getName();

        if(!COMMANDS_CACHE_MAP.containsKey(serverName))
            COMMANDS_CACHE_MAP.put(serverName, new CommandsCache().reverse());

        CommandsCache commandsCache = COMMANDS_CACHE_MAP.get(serverName);
        List<String> commandsAsString = new ArrayList<>();
        HashMap<String, Command> commandsMap = new HashMap<>(event.getCommands());

        event.getCommands().forEach((key, value) -> commandsAsString.add(key));
        commandsCache.handleCommands(commandsAsString, serverName);

        if(PermissionUtil.hasBypassPermission(player)) return;

        List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);
        event.getCommands().entrySet().removeIf(command -> playerCommands.contains(command.getKey()));

        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.call(player.getUniqueId(), new ArrayList<>(event.getCommands().keySet()));
        if(filteredSuggestionEvent.isCancelled()) event.getCommands().clear();

        for (String commandName : filteredSuggestionEvent.getSuggestions()) {
            if(!event.getCommands().containsKey(commandName)) event.getCommands().put(commandName, commandsMap.get(commandName));
        }

        commandsMap = null;
    }

    public static void updateCommands() {
        COMMANDS_CACHE_MAP.values().forEach(CommandsCache::reset);
    }
}
