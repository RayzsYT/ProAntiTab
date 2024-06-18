package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.permission.PermissionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        event.getSuggestions().removeIf(command -> command.startsWith("/") && Storage.Blacklist.isBlocked(player, command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, player.getCurrentServer().get().getServerInfo().getName()));
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

        final List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);
        event.getRootNode().getChildren().removeIf(command -> command == null || command.getName() == null || playerCommands.contains(command.getName()));
    }

    public static void updateCommands() {
        COMMANDS_CACHE_MAP.values().forEach(CommandsCache::reset);
    }
}
