package de.rayzs.pat.plugin.listeners.velocity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.permission.PermissionUtil;

public class VelocityAntiTabListener {

    private static ProxyServer server;

    public VelocityAntiTabListener(ProxyServer server) {
        VelocityAntiTabListener.server = server;
    }

    @Subscribe (order = PostOrder.LAST)
    public void onTabComplete(TabCompleteEvent event) {
        Player player = event.getPlayer();
        String serverName = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "unknown";

        if (Storage.Blacklist.isDisabledServer(serverName))
            return;

        if (PermissionUtil.hasBypassPermission(player) || event.getSuggestions().isEmpty() || !player.getCurrentServer().isPresent()) return;

        event.getSuggestions().removeIf(command -> {
            if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(command) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(command)) {
                return false;
            }

            return !Storage.Blacklist.canPlayerAccessTab(player, command, serverName);
        });

        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.callFilteredSuggestionEvents(player, event.getSuggestions());
        if(filteredSuggestionEvent.isCancelled()) event.getSuggestions().clear();
    }

    @Subscribe (order = PostOrder.LAST)
    public void onPlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        try {
            Player player = event.getPlayer();

            if (event.getRootNode().getChildren().isEmpty() || !player.getCurrentServer().isPresent()) {
                return;
            }

            String serverName = player.getCurrentServer().get().getServer().getServerInfo().getName();

            Map<String, CommandsCache> cache = Storage.getLoader().getCommandsCacheMap();

            if (!cache.containsKey(serverName)) {
                cache.put(serverName, new CommandsCache());
            }

            CommandsCache commandsCache = cache.get(serverName);

            List<String> commandsAsString = new ArrayList<>();
            event.getRootNode().getChildren().stream().filter(command -> command != null && command.getName() != null).forEach(command -> commandsAsString.add(command.getName()));
            commandsCache.handleCommands(commandsAsString, serverName);

            if (PermissionUtil.hasBypassPermission(player)) {
                return;
            }

            final boolean newer = player.getProtocolVersion().getProtocol() > 340, argsChildrenExist = event.getRootNode().getChild("args") != null;
            final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);

            if (event.getRootNode().getChildren().size() == 1
                    && newer && argsChildrenExist
                    && event.getRootNode().getChild("args").getChildren().isEmpty()) {
                return;
            }

            if (event.getRootNode().getChildren().size() == 1 && newer && argsChildrenExist) {
                return;
            }

            event.getRootNode().getChildren().removeIf(command -> {
                if (command == null || command.getName() == null)
                    return true;

                if (command.getName().equals("args"))
                    return false;

                if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.isTabCompletable(command.getName()) || Storage.ConfigSections.Settings.CUSTOM_VERSION.isTabCompletable(command.getName())) {
                    return false;
                }

                return !playerCommands.contains(command.getName());
            });
        } catch (Exception exception) {
            System.out.println("An error occurred while processing commands in PAT: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}
