package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.permission.PermissionUtil;
import io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        event.getCommands().forEach((key, value) -> commandsAsString.add(key));
        commandsCache.handleCommands(commandsAsString);

        if(PermissionUtil.hasBypassPermission(player)) return;

        List<String> playerCommands = commandsCache.getPlayerCommands(commandsAsString, player, player.getUniqueId(), serverName);
        event.getCommands().entrySet().removeIf(command -> playerCommands.contains(command.getKey()));

    }

    public static void updateCommands() {
        COMMANDS_CACHE_MAP.values().forEach(CommandsCache::reset);
    }
}
