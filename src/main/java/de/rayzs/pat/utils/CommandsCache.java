package de.rayzs.pat.utils;

import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.api.storage.Storage;
import java.util.*;

public class CommandsCache {

    private List<String> filteredCommands = null, allCommands = null;
    private boolean useList = true;
    private boolean change = false;

    public CommandsCache reverse() {
        this.useList = !useList;
        return this;
    }

    public void handleCommands(List<String> commands) {
        if(change) return;
        if(!isOutdated(commands)) return;

        filteredCommands = new LinkedList<>();
        allCommands = new ArrayList<>(commands);

        for (String command : allCommands) {
            if (filteredCommands.contains(command)) continue;

            if (useList) {
                if (Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false))
                    continue;

                filteredCommands.add(command);
                continue;
            }

            if (!Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false))
                filteredCommands.add(command);
        }

        change = true;
    }

    public void handleCommands(List<String> commands, String server) {
        if(change) return;
        if(!isOutdated(commands)) return;
        server = server.toLowerCase();

        filteredCommands = new LinkedList<>();
        allCommands = new ArrayList<>(commands);

        for (String command : allCommands) {
            if (filteredCommands.contains(command)) continue;

            if (useList) {
               if(Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, server))
                    continue;

                filteredCommands.add(command);
                continue;
            }

            if(!Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, server))
                filteredCommands.add(command);

            change = true;
        }
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid) {
        return getPlayerCommands(unfilteredCommands, targetObj, uuid, null);
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid, String serverName) {
        if(serverName != null) serverName = serverName.toLowerCase();

        List<String> playerCommands = new LinkedList<>();

        if(filteredCommands == null)
            return useList ? playerCommands : new LinkedList<>(unfilteredCommands);

        if(useList && !Storage.Blacklist.isOnIgnoredServer(serverName)) playerCommands = new LinkedList<>(filteredCommands);

        boolean permitted;

        if (!(Storage.USE_LUCKPERMS && !LuckPermsAdapter.hasAnyPermissions(uuid))) {
            for (String command : unfilteredCommands) {
                if (playerCommands.contains(command)) continue;

                permitted = Reflection.isProxyServer() && serverName != null
                        ? !Storage.Blacklist.isBlocked(targetObj, command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, serverName)
                        : PermissionUtil.hasBypassPermission(targetObj, command, false);

                if(useList && !permitted || !useList && permitted) continue;
                playerCommands.add(command);
            }
        }

        return playerCommands;
    }

    public void updateChangeState() {
        change = false;
    }

    public void reset() {
        if(change) return;
        filteredCommands = null;
    }

    public boolean isOutdated(List<String> commands) {
        return filteredCommands == null || !ArrayUtils.compareStringArrays(commands, allCommands);
    }
}
