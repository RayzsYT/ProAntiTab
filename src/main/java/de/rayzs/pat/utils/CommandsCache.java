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

        LinkedList<String> tmpFilteredCommands = new LinkedList<>();
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

        filteredCommands = tmpFilteredCommands;
        change = true;
    }

    public void handleCommands(List<String> commands, String server) {
        if(!isOutdated(commands)) 
            return;

        LinkedList<String> tmpFilteredCommands = new LinkedList<>();
        allCommands = new ArrayList<>(commands);

        for (String command : allCommands) {
            command = StringUtils.getFirstArg(command);

            Storage.Blacklist.BlockType type = Storage.Blacklist.BlockTypeFetcher.getType(command);
            if (type != Storage.Blacklist.BlockType.BOTH && type != Storage.Blacklist.BlockType.TAB)
                continue;

            if (isFilterListAvailable()) {
                if (filteredCommands.contains(command)) 
                    continue;

                if (!Storage.Blacklist.isBlockedTab(command, server))
                    filteredCommands.add(command);
            }

        }

        filteredCommands = tmpFilteredCommands;
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid) {
        return getPlayerCommands(unfilteredCommands, targetObj, uuid, null);
    }

    public List<String> getPlayerCommands(Collection<String> unfilteredCommands, Object targetObj, UUID uuid, String serverName) {
        List<String> playerCommands = new LinkedList<>(unfilteredCommands);
        List<String> localFilteredCommands = filteredCommands == null ? null : new LinkedList<>();

        if (localFilteredCommands == null)
            return playerCommands;


        final int max = filteredCommands.size();
        for (int i = 0; i < max; i++) {
            try {
                String command = filteredCommands.get(i);
                filteredCommands.add(command);
            } catch (ArrayIndexOutOfBoundsException outOfBoundsException) {
                Logger.warning("Array is out of bounds " + i + "/" + max + "! " + outOfBoundsException.getMessage());
                break;
            }
        }


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
        return this.filteredCommands != null;
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
