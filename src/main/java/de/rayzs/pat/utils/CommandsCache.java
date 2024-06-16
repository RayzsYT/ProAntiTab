package de.rayzs.pat.utils;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.utils.permission.PermissionUtil;
import java.util.*;

public class CommandsCache {

    private List<String> filteredCommands = null, allCommands = null;
    private boolean useList = true;

    public CommandsCache reverse() {
        this.useList = !useList;
        return this;
    }

    public void handleCommands(List<String> commands) {
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
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid) {
        return getPlayerCommands(unfilteredCommands, targetObj, uuid, null);
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid, String serverName) {
        List<String> playerCommands = new LinkedList<>();
        if(filteredCommands == null)
            return useList ? playerCommands : new LinkedList<>(unfilteredCommands);

        if(useList) playerCommands = new LinkedList<>(filteredCommands);

        boolean permitted;
        if (!(Storage.USE_LUCKPERMS && !LuckPermsAdapter.hasAnyPermissions(uuid))) {
            for (String command : unfilteredCommands) {
                if (filteredCommands.contains(command)) continue;
                permitted = Reflection.isProxyServer() && serverName != null
                        ? !Storage.Blacklist.isBlocked(targetObj, command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, serverName)
                        : PermissionUtil.hasBypassPermission(targetObj, command);

                if(useList && !permitted || !useList && permitted) continue;
                playerCommands.add(command);
            }
        }

        return playerCommands;
    }

    public void reset() {
        filteredCommands = null;
    }

    public boolean isOutdated(List<String> commands) {
        return filteredCommands == null || allCommands == null || !Arrays.equals(commands.toArray(), allCommands.toArray());
    }
}
