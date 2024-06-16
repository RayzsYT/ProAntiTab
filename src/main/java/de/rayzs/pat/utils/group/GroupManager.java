package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.*;
import java.util.*;

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

    public static boolean canAccessCommand(Object targetObj, String command) {
        command = Storage.Blacklist.getBlacklist().convertCommand(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false);

        final List<Group> finalList = new ArrayList<>(GROUPS);
        for (Group group : finalList) {
            if(group == null) continue;

            if (group.contains(command))
                if (group.hasPermission(targetObj)) return true;
        }
        return false;
    }

    public static boolean canAccessCommand(Object targetObj, String command, String server) {
        command = Storage.Blacklist.getBlacklist().convertCommand(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false);
        server = server.toLowerCase();

        final List<Group> finalList = new ArrayList<>(GROUPS);
        for (Group group : finalList) {
            if(group == null) continue;

            if (group.contains(command, server))
                if (group.hasPermission(targetObj)) return true;
        }
        return false;
    }

    public static void setGroup(String groupName, List<String> commands) {
        if(groupName.length() < 1) return;
        Group group = registerAndGetGroup(groupName);
        group.setCommands(commands);
    }

    public static Group registerAndGetGroup(String groupName) {
        Group group = getGroupByName(groupName);
        if(group != null) return group;
        group = new Group(groupName);
        GROUPS.add(group);
        return group;
    }

    public static void registerGroup(String groupName) {
        if(isGroupRegistered(groupName)) return;
        GROUPS.add(new Group(groupName));
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
        if(groupName == null) return;
        group.remove(command);
    }

    public static void removeFromGroup(String groupName, String command, String server) {
        Group group = getGroupByName(groupName);
        if(groupName == null) return;
        group.remove(command, server);
    }

    public static void unregisterGroup(String groupName) {
        unregisterGroup(groupName, null);
    }

    public static void unregisterGroup(String groupName, String server) {
        Group group = getGroupByName(groupName);
        if(group == null) return;
        if(server != null) group.deleteGroup(server);
        else group.deleteGroup();
        GROUPS.remove(group);
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

    public static List<Group> getGroupsByCommandAndServer(String command, String server) {
        List<Group> result = new ArrayList<>();
        GROUPS.stream().filter(group -> group.getAllServerGroupBlacklist(server).stream().anyMatch(groupBlacklist -> groupBlacklist.isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false))).forEach(result::add);
        return result;
    }

    public static List<Group> getGroupsByServer(String server) {
        List<Group> result = new ArrayList<>();

        GROUPS.stream().filter(group -> {
            if (BlacklistCreator.exist(group.getGroupName(), server)) {
                GroupBlacklist groupBlacklist = group.getOrCreateGroupBlacklist(server);
                return (groupBlacklist != null && !groupBlacklist.getCommands().isEmpty() && groupBlacklist.getCommands().size() >= 1);
            }

            return false;
        }).forEach(result::add);

        return result;
    }

    public static List<String> getGroupsByNameOnlyIncludingCommand(String command) {
        List<String> result = new ArrayList<>();
        GROUPS.stream().filter(group -> group.contains(command)).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static List<String> getGroupsByNameOnlyIncludingCommand(String command, String server) {
        List<String> result = new ArrayList<>();
        GROUPS.stream().filter(group -> group.contains(command, server)).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static List<String> getGroupsByNameNotIncludingCommand(String command) {
        List<String> result = new ArrayList<>();
        GROUPS.stream().filter(group -> !group.contains(command)).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static List<String> getGroupsByNameNotIncludingCommand(String command, String server) {
        List<String> result = new ArrayList<>();
        GROUPS.stream().filter(group -> !group.contains(command, server)).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static TinyGroup convertToTinyGroup(String groupName, List<String> commands) {
        return new TinyGroup(groupName, commands);
    }

    public static void clearAllGroups() {
        GROUPS.clear();
    }

    public static boolean isGroupRegistered(String groupName) {
        return getGroupByName(groupName) != null;
    }
}
