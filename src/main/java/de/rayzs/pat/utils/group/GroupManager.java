package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.Storage;
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

        int priority = -1;
        final List<Group> finalList = new ArrayList<>(GROUPS);

        if(finalList.isEmpty()) return false;
        for (Group group : finalList) {
            if(group == null) continue;
            if (priority == -1 || group.getPriority() <= priority) {

                if (group.hasPermission(targetObj)) {
                    priority = group.getPriority();
                    if (group.contains(command)) return true;
                }
            }
        }
        return false;
    }

    public static boolean canAccessCommand(Object targetObj, String command, boolean slash) {
        command = Storage.Blacklist.getBlacklist().convertCommand(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false, slash);

        int priority = -1;
        final List<Group> finalList = new ArrayList<>(GROUPS);

        if(finalList.isEmpty()) return false;
        for (Group group : finalList) {
            if(group == null) continue;
            if (priority == -1 || group.getPriority() <= priority) {

                if (group.hasPermission(targetObj)) {
                    priority = group.getPriority();
                    if (group.contains(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false)) return true;
                }
            }
        }
        return false;
    }

    public static boolean canAccessCommand(Object targetObj, String command, String server) {
        command = Storage.Blacklist.getBlacklist().convertCommand(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false);
        server = server.toLowerCase();

        final List<Group> finalList = new ArrayList<>(GROUPS);

        if(finalList.isEmpty())
            return false;

        for (Group group : finalList) {
            if(group == null) continue;

            if (group.contains(command, server))
                if (group.hasPermission(targetObj)) return true;
        }
        return false;
    }

    public static boolean canAccessCommand(Object targetObj, String command, boolean slash, String server) {
        command = Storage.Blacklist.getBlacklist().convertCommand(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, false,  slash);
        server = server.toLowerCase();

        final List<Group> finalList = new ArrayList<>(GROUPS);

        if(finalList.isEmpty())
            return false;

        for (Group group : finalList) {
            if(group == null) continue;

            if (group.contains(command, server))
                if (group.hasPermission(targetObj)) return true;
        }
        return false;
    }

    public static void setGroup(String groupName, List<String> commands) {
        setGroup(groupName, 1, commands);
    }

    public static void setGroup(String groupName, int priority, List<String> commands) {
        if(groupName.length() < 1) return;
        Group group = registerAndGetGroup(groupName, priority);
        group.setCommands(commands);
    }

    public static Group registerAndGetGroup(String groupName) {
        return registerAndGetGroup(groupName, 1);
    }

    public static Group registerAndGetGroup(String groupName, int priority) {
        Group group = getGroupByName(groupName);
        if(group != null) return group;
        group = new Group(groupName, priority);
        GROUPS.add(group);
        sort();
        return group;
    }

    public static void sort() {
        GROUPS.sort(Comparator.comparingInt(Group::getPriority));
    }

    public static void registerGroup(String groupName) {
        if(isGroupRegistered(groupName)) return;
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
