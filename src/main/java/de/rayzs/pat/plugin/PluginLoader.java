package de.rayzs.pat.plugin;

import de.rayzs.pat.utils.CommandsCache;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface PluginLoader {

    void handleReload();
    boolean doesCommandExist(String command);

    HashMap<String, CommandsCache> getCommandsCacheMap();

    void updateCommandCache();

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

    List<String> getPluginNames();
}
