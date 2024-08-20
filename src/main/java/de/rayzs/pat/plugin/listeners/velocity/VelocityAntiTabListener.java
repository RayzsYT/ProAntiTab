package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandsCache;
import com.velocitypowered.api.proxy.*;
import com.velocitypowered.api.event.*;
import java.util.*;

public class VelocityAntiTabListener {

    private static ProxyServer server;
    private static final HashMap<String, CommandsCache> COMMANDS_CACHE_MAP = new HashMap<>();

    public VelocityAntiTabListener(ProxyServer server) {
        VelocityAntiTabListener.server = server;
    }

    @Subscribe (order = PostOrder.LAST)
    public void onTabComplete(TabCompleteEvent event) {
        Player player = event.getPlayer();
        if(PermissionUtil.hasBypassPermission(player) || event.getSuggestions().isEmpty() || !player.getCurrentServer().isPresent()) return;

        event.getSuggestions().removeIf(command -> Storage.Blacklist.isBlocked(player, command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getCurrentServer().get().getServerInfo().getName()));
        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.call(player.getUniqueId(), event.getSuggestions());
        if(filteredSuggestionEvent.isCancelled()) event.getSuggestions().clear();
    }

    @Subscribe (order = PostOrder.LAST)
    public void onPlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if(event.getRootNode().getChildren().isEmpty() || !player.getCurrentServer().isPresent()) return;
        String serverName = player.getCurrentServer().get().getServer().getServerInfo().getName();

        if(!COMMANDS_CACHE_MAP.containsKey(serverName))
            COMMANDS_CACHE_MAP.put(serverName, new CommandsCache().reverse());
        CommandsCache commandsCache = COMMANDS_CACHE_MAP.get(serverName);

        List<String> commandsAsString = new ArrayList<>();
        event.getRootNode().getChildren().stream().filter(command -> command != null && command.getName() != null).forEach(command -> commandsAsString.add(command.getName()));
        commandsCache.handleCommands(commandsAsString, serverName);

        if(PermissionUtil.hasBypassPermission(player)) return;

        /*
        ConcurrentHashMap<String, CommandNode<?>> commandsMap = new ConcurrentHashMap<>();
        for (CommandNode<?> child : event.getRootNode().getChildren()) {
            if(child == null || child.getName() == null) continue;
            commandsMap.put(child.getName(), child);
        }

        ConcurrentHashMap<String, CommandNode<?>> filteredCommandsMap = new ConcurrentHashMap<>(commandsMap);
        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);
        event.getRootNode().getChildren().removeIf(command -> {
            if (command == null || command.getName() == null || playerCommands.contains(command.getName())) {
                if(command != null && command.getName() != null) filteredCommandsMap.remove(command.getName());
                return true;
            }
            return false;
        });*/

        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);
        event.getRootNode().getChildren().removeIf(command -> command == null || command.getName() == null || playerCommands.contains(command.getName()));

        /*
        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.call(player.getUniqueId(), new ArrayList<>(filteredCommandsMap.keySet()));
        if(filteredSuggestionEvent.isCancelled()) event.getRootNode().getChildren().clear();

        for (String commandName : filteredSuggestionEvent.getSuggestions()) {
            CommandNode<?> commandNode = commandsMap.get(commandName);
            event.getRootNode().getChildren().add(commandNode);
            if(!filteredCommandsMap.containsKey(commandName)) event.getRootNode().addChild(commandNode);
        }

        commandsMap = null;*/
    }

    public static void updateCommands() {
        COMMANDS_CACHE_MAP.values().forEach(CommandsCache::reset);
    }
}
