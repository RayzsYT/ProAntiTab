package de.rayzs.pat.utils.group;

import de.rayzs.pat.utils.*;
import java.util.*;

public class GroupManager {

    private static final List<Group> GROUPS = new ArrayList<>();

    public static void initialize() {
        Storage.STORAGE.getKeys(true).stream().filter(key -> !key.equals("commands")).forEach(GroupManager::registerGroup);
    }

    public static boolean canAccessCommand(Object targetObj, String command) {
        return GROUPS.stream().filter(group -> group.contains(command) && group.hasPermission(targetObj)
        ).findFirst().orElse(null) != null;
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

    public static void removeFromGroup(String groupName, String command) {
        Group group = getGroupByName(groupName);
        if(groupName == null) return;
        group.remove(command);
    }

    public static void unregisterGroup(String groupName) {
        Group group = getGroupByName(groupName);
        if(group == null) return;
        Storage.STORAGE.setAndSave(group.getGroupName(), null);
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

    public static List<String> getGroupByNameOnlyIncludingCommand(String command) {
        List<String> result = new ArrayList<>();
        GROUPS.stream().filter(group -> group.contains(command)).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static List<String> getGroupByNameNotIncludingCommand(String command) {
        List<String> result = new ArrayList<>();
        GROUPS.stream().filter(group -> !group.contains(command)).forEach(group -> result.add(group.getGroupName()));
        return result;
    }

    public static void clearAllGroups() {
        GROUPS.clear();
    }

    public static boolean isGroupRegistered(String groupName) {
        return getGroupByName(groupName) != null;
    }
}
