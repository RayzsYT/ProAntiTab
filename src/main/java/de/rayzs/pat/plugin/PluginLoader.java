package de.rayzs.pat.plugin;

import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface PluginLoader {

    void handleReload();
    boolean doesCommandExist(String command);

    /**
     * Updates commands for all players.
     * Requires PAT sync on proxy for it to work!
     * Non-proxy servers do not require anything additionally.
     */
    void updateCommands();

    /**
     * Updates commands for a certain player.
     * Requires PAT sync on proxy for it to work!
     * Non-proxy servers do not require anything additionally.
     */
    void updateCommands(CommandSender sender);

    HashMap<String, CommandsCache> getPerServerCommandsCacheMap();
    CommandsCache getBukkitCommandsCacheMap();

    void delayedPermissionsReload();

    void delayedPermissionsReload(CommandSender sender);

    void resetCommandsCache();

    List<String> getServerNames();

    Object getConsoleSender();

    Object getPlayerObjByName(String name);
    Object getPlayerObjByUUID(UUID uuid);

    UUID getUUIDByName(String playerName);
    String getNameByUUID(UUID uuid);

    String getPlayerServerName(UUID uuid);

    boolean isPlayerOnline(String playerName);
    boolean doesPlayerExist(String playerName);

    List<String> getOnlinePlayerNames(String server);

    List<UUID> getPlayerIdsByServer(String server);
    List<UUID> getPlayerIds();

    List<String> getPlayerNames();
    List<String> getOnlinePlayerNames();
    List<String> getOfflinePlayerNames();

    List<String> getAllCommands(boolean useColons);
    List<String> getPluginCommands(String pluginName, boolean useColons);

    /**
     * <pre>
     * %n = Plugin name
     * %v = Plugin version
     * </pre>
     */
    List<String> getFormattedPluginNames(String format);
}
