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
        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.callFilteredSuggestionEvents(player, event.getSuggestions());
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

        final boolean newer = player.getProtocolVersion().getProtocol() > 340, argsChildrenExist = event.getRootNode().getChild("args") != null;
        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);

        if(event.getRootNode().getChildren().size() == 1 && newer
                && argsChildrenExist
                && event.getRootNode().getChild("args").getChildren().isEmpty()) {
            return;
        }

        if(event.getRootNode().getChildren().size() == 1 && newer && argsChildrenExist) return;

        event.getRootNode().getChildren().removeIf(command -> {
            if (command == null || command.getName() == null)
                return true;

            if (command.getName().equals("args"))
                return false;

            return playerCommands.contains(command.getName());
        });
    }

    public static void updateCommands() {
        new ArrayList<>(COMMANDS_CACHE_MAP.values()).forEach(CommandsCache::reset);
    }
}
