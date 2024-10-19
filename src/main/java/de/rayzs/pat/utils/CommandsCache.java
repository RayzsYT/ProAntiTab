package de.rayzs.pat.utils;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.plugin.logger.Logger;
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
            if (isFilterListAvailable() && filteredCommands.contains(command)) continue;

            if (useList) {
                if (Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false))
                    continue;

                if(isFilterListAvailable()) filteredCommands.add(command);
                continue;
            }

            if (!Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false))
                if(isFilterListAvailable()) filteredCommands.add(command);
        }

        change = true;
    }

    public void handleCommands(List<String> commands, String server) {
        if(!isOutdated(commands)) return;
        server = server.toLowerCase();

        filteredCommands = new LinkedList<>();
        allCommands = new ArrayList<>(commands);

        for (String command : allCommands) {
            if (isFilterListAvailable(server) && filteredCommands.contains(command)) continue;

            if (useList) {
               if(Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, server))
                    continue;

                if(isFilterListAvailable(server)) filteredCommands.add(command);
                continue;
            }

            if(!Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, server))
                if(isFilterListAvailable(server)) filteredCommands.add(command);
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

        boolean permitted, bypassNamespace = true, turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;

        if(!turn && Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.ENABLED)
            bypassNamespace = Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(targetObj);

        if (!(Storage.USE_LUCKPERMS && !LuckPermsAdapter.hasAnyPermissions(uuid))) {
            for (String command : unfilteredCommands) {

                if(useList && !turn && !bypassNamespace && Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command)) {
                    playerCommands.remove(command);
                    continue;
                }

                if (playerCommands.contains(command)) continue;

                permitted = Reflection.isProxyServer() && serverName != null
                        ? !Storage.Blacklist.isBlocked(targetObj, command, !turn, serverName)
                        : !Storage.Blacklist.isBlocked(targetObj, command, !turn, false, false);

                    if (useList && !permitted || !useList && permitted)
                        continue;

                playerCommands.add(command);
            }
        }

        String uuidSubstring = uuid.toString().substring(uuid.toString().length() - 5);
        if(playerCommands.isEmpty())
            Logger.debug("Commands list for player with uuid " + uuidSubstring + " is empty! (" + (filteredCommands != null ? filteredCommands.size() : "null") + " | " + (unfilteredCommands != null ?  unfilteredCommands.size() : "null") + ")");
        else
            Logger.debug("Created list of commands for player with uuid " + uuidSubstring + " with a total of " + playerCommands.size() + " commands!");

        PATEventHandler.callUpdatePlayerCommandsEvents(targetObj, playerCommands, serverName != null);
        return playerCommands;
    }

    public boolean isFilterListAvailable() {
        return isFilterListAvailable(null);
    }

    public boolean isFilterListAvailable(String server) {
        boolean available = this.filteredCommands != null;
        if(!available) Logger.debug("FilterList didn't exist during built!" + (server != null ? "(server: " + server + ")" : ""));
        return available;
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
