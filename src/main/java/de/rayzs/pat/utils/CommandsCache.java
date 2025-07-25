package de.rayzs.pat.utils;

import java.util.*;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;

public class CommandsCache {

    private List<String> filteredCommands = null, allCommands = null;
    private boolean change = false;

    public void handleCommands(List<String> commands) {
        if (change || !isOutdated(commands)) 
            return;

        filteredCommands = new LinkedList<>();
        allCommands = new ArrayList<>(commands);

        for (String command : allCommands) {
            command = StringUtils.getFirstArg(command);

            Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(command);
            if (type != Storage.Blacklist.BlockType.BOTH && type != Storage.Blacklist.BlockType.TAB)
                continue;
            
            if (isFilterListAvailable()) {
                if (filteredCommands.contains(command)) 
                    continue;

                if (!Storage.Blacklist.isBlockedTab(command)) {
                    filteredCommands.add(command);
                }
            }

        }

        change = true;
    }

    public void handleCommands(List<String> commands, String server) {
        if(!isOutdated(commands)) 
            return;

        filteredCommands = new LinkedList<>();
        allCommands = new ArrayList<>(commands);

        for (String command : allCommands) {
            command = StringUtils.getFirstArg(command);

            Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(command);
            if (type != Storage.Blacklist.BlockType.BOTH && type != Storage.Blacklist.BlockType.TAB)
                continue;

            if (isFilterListAvailable(server)) {
                if (filteredCommands.contains(command)) 
                    continue;

                if (!Storage.Blacklist.isBlockedTab(command, server))
                    filteredCommands.add(command);
            }

        }
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid) {
        return getPlayerCommands(unfilteredCommands, targetObj, uuid, null);
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid, String serverName) {
        List<String> playerCommands = new LinkedList<>(unfilteredCommands);
        List<String> localFilteredCommands = filteredCommands == null ? null : new LinkedList<>(filteredCommands);

        if (localFilteredCommands == null)
            return playerCommands;
        

        boolean hasNamespaceBypass = Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.doesBypass(targetObj);

        playerCommands.removeIf(command -> {
            if (!hasNamespaceBypass && Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.isCommand(command)) {
                return true;
            }

            if (localFilteredCommands.contains(command)) {
                return false;
            }

            return !Storage.Blacklist.canPlayerAccessTab(targetObj, command, serverName);
        });

        String uuidSubstring = uuid.toString().substring(uuid.toString().length() - 5);
        if(playerCommands.isEmpty())
            Logger.debug("Commands list for player with uuid " + uuidSubstring + " is empty! (" + (filteredCommands != null ? filteredCommands.size() : "null") + " | " + (unfilteredCommands != null ?  unfilteredCommands.size() : "null") + ")");
        else
            Logger.debug("Created list of commands for player with uuid " + uuidSubstring + " with a total of " + playerCommands.size() + " commands!");

        PATEventHandler.callUpdatePlayerCommandsEvents(targetObj, playerCommands, serverName != null);

        return playerCommands.stream().map(command -> {
            command = StringUtils.getFirstArg(command);

            Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(command);
            if (type == null || type == Storage.Blacklist.BlockType.BOTH)
                return command;

            return Storage.Blacklist.BlockTypeFetcher.modify(command, type);
        }).toList();

    }

    public boolean isFilterListAvailable() {
        return isFilterListAvailable(null);
    }

    public boolean isFilterListAvailable(String server) {
        boolean available = this.filteredCommands != null;
        if(!available) 
            Logger.debug("FilterList didn't exist during built!" + (server != null ? "(server: " + server + ")" : ""));
        
        return available;
    }

    public void updateChangeState() {
        change = false;
    }

    public void reset() {
        // Removed to reset the change state. Experimental change though
        //if(change) return;
        // Prob. gonna change it to: if (CONNECTED_TO_BUNGEE && change) return;
        updateChangeState();
        filteredCommands = null;
    }

    public boolean isOutdated(List<String> commands) {
        return filteredCommands == null || !ArrayUtils.compareStringArrays(commands, allCommands);
    }
}
