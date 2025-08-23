package de.rayzs.pat.utils.group;

import java.util.*;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.Storage.Blacklist.BlockType;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;

public class GroupManager {

    private static final List<Group> GROUPS = new LinkedList<>();

    public static void initialize() {
        Storage.Files.STORAGE.getKeys("groups", false).forEach(GroupManager::registerGroup);
        if(!Reflection.isProxyServer()) return;
        getGroups().forEach(group -> {
            Storage.Files.STORAGE.getKeys("groups." + group.getGroupName() + ".servers", false).forEach(key -> {
                group.getOrCreateGroupBlacklist(key, true);
            });
        });
    }

    public static List<Group> getPlayerGroups(Object player) {
        final int invalidPriority = Integer.MAX_VALUE -1;

        List<Group> playerGroups = new ArrayList<>(GroupManager.getGroups().stream().filter(group -> group.hasPermission(player)).toList());

        int priority = playerGroups.stream()
                                .mapToInt(Group::getPriority)
                                .filter(group -> group <= invalidPriority)
                                .min().orElse(invalidPriority);
        
        playerGroups.removeIf(group -> group.getPriority() > priority);

        return playerGroups;
    }

    public static List<Group> getPlayerGroups(UUID uuid) {
        final int invalidPriority = Integer.MAX_VALUE -1;

        List<Group> playerGroups = new ArrayList<>(GroupManager.getGroups().stream().filter(group -> group.hasPermission(uuid)).toList());

        int priority = playerGroups.stream()
                .mapToInt(Group::getPriority)
                .filter(group -> group <= invalidPriority)
                .min().orElse(invalidPriority);

        playerGroups.removeIf(group -> group.getPriority() > priority);

        return playerGroups;
    }

    public static boolean canAccessCommand(Object targetObj, String unmodifiedCommand, Storage.Blacklist.BlockType type) {
        return canAccessCommand(targetObj, unmodifiedCommand, type, null);
    }

    public static boolean canAccessCommand(Object targetObj, String unmodifiedCommand, Storage.Blacklist.BlockType type, String server) {
        final CommandSender sender = CommandSenderHandler.from(targetObj);

        if (sender == null) {
            Logger.warning("Failed to load player! (GroupManager#62)");
            return false;
        }

        final List<Group> playerGroups = sender.getGroups();

        if (playerGroups == null || playerGroups.isEmpty()) {
            return false;
        }
        
        String command = type.toString() + unmodifiedCommand;

        boolean permitted = false;
        for (Group group : playerGroups) {

            if (server != null) {

                List<String> servers = group.getBlacklistServerNames(server);

                for (String s : servers) {
                    if (!group.contains(command, s)) {
                        continue;
                    }

                    permitted = true;
                    break;
                }

            } else {
                permitted = group.contains(command);
            }


            if (permitted) {
                break;
            }

        }

        if (!permitted && type != BlockType.BOTH)
            return canAccessCommand(targetObj, unmodifiedCommand, Storage.Blacklist.BlockType.BOTH, server);
        
        return permitted;
    }

    public static void setGroup(String groupName, List<String> commands) {
        setGroup(groupName, 1, commands);
    }

    public static void setGroup(String groupName, int priority, List<String> commands) {
        if(groupName.isEmpty())
            return;

        Group group = registerAndGetGroup(groupName, priority);
        group.setCommands(commands);
    }

    public static Group registerAndGetGroup(String groupName) {
        return registerAndGetGroup(groupName, 1);
    }

    public static Group registerAndGetGroup(String groupName, int priority) {
        Group group = getGroupByName(groupName);
        
        if(group != null) 
            return group;
        
        group = new Group(groupName, priority);
        GROUPS.add(group);
        
        sort();

        return group;
    }

    public static void sort() {
        GROUPS.sort(Comparator.comparingInt(Group::getPriority));
    }

    public static void registerGroup(String groupName) {
        if(isGroupRegistered(groupName)) 
            return;
            
        GROUPS.add(new Group(groupName));
        sort();
    }

    public static void addToGroup(String groupName, String command) {
        Group group = getGroupByName(groupName);
        if(groupName == null) return;
        group.add(command);
    }

    public static void addToGroup(String groupName, String command, String server) {
        Group group = getGroupByName(groupName);
        if(groupName == null) return;
        group.add(command, server);
    }

    public static void removeFromGroup(String groupName, String command) {
        Group group = getGroupByName(groupName);

        if(groupName == null) 
            return;
        
        group.remove(command);
    }

    public static void removeFromGroup(String groupName, String command, String server) {
        Group group = getGroupByName(groupName);

        if(groupName == null) 
            return;
        
        group.remove(command, server);
    }

    public static void unregisterGroup(String groupName) {
        unregisterGroup(groupName, null);
    }

    public static void unregisterGroup(String groupName, String server) {
        Group group = getGroupByName(groupName);
        
        if(group == null) 
            return;

        if(server != null) 
            group.deleteGroup(server);
        else 
            group.deleteGroup();

        GROUPS.remove(group);
        sort();
    }

    public static Group getGroupByName(String groupName) {
        return GROUPS.stream().filter(group -> group.getGroupName().equals(groupName)).findFirst().orElse(null);
    }

    public static List<Group> getGroups() {
        return GROUPS;
    }

    public static List<String> getGroupNames() {
        List<String> result = new ArrayList<>();
        GROUPS.forEach(group -> result.add(group.getGroupName()));
        return result;
    }


    public static List<String> getGroupNamesByServer(String server) {
        List<String> result = new ArrayList<>();
        getGroupsByServer(server).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static void clearServerGroupBlacklists() {
        GROUPS.forEach(Group::clearServerGroupBlacklistsCache);
    }

    public static List<Group> getGroupsByServer(String server) {
        List<Group> result = new ArrayList<>();

        GROUPS.stream().filter(group -> {

            if (BlacklistCreator.exist(group.getGroupName(), server)) {
                GroupBlacklist groupBlacklist = group.getOrCreateGroupBlacklist(server);
                return (groupBlacklist != null && groupBlacklist.getCommands().size() >= 1);
            }

            return false;

        }).forEach(result::add);

        return result;
    }

    public static TinyGroup convertToTinyGroup(String groupName, int priority, List<String> commands) {
        return new TinyGroup(groupName, priority, commands);
    }

    public static void clearAllGroups() {
        GROUPS.clear();
    }

    public static boolean isGroupRegistered(String groupName) {
        return getGroupByName(groupName) != null;
    }
}
