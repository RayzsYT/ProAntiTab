package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.utils.PermissionUtil;

import java.io.Serializable;
import java.util.*;

public class Group implements Serializable {

    private final HashMap<String, GroupBlacklist> groupServerBlacklist = new HashMap<>();
    private final GroupBlacklist generalGroupBlacklist;
    private final String groupName;

    public Group(String groupName) {
        this.groupName = groupName;
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.load();
    }

    public Group(String groupName, List<String> commands) {
        this.groupName = groupName;
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.setList(commands);
    }

    public String getGroupName() {
        return groupName;
    }

    public void add(String command) {
        generalGroupBlacklist.add(command).save();
    }

    public void add(String command, String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
        serverGroupBlacklist.load();
        serverGroupBlacklist.add(command).save();
    }

    public void remove(String command) {
        generalGroupBlacklist.remove(command).save();
    }

    public void remove(String command, String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
        serverGroupBlacklist.load();
        serverGroupBlacklist.remove(command).save();
    }

    public void clear() {
        generalGroupBlacklist.clear().save();
    }

    public void clear(String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
        serverGroupBlacklist.clear().save();
        serverGroupBlacklist.load();
        Storage.Files.STORAGE.reload();
    }

    public void setCommands(List<String> commands) {
        generalGroupBlacklist.setList(commands);
    }

    public boolean hasPermission(Object targetObj) {
        return PermissionUtil.hasPermission(targetObj, "group." + groupName) || PermissionUtil.hasServerPermission(targetObj, "group." + groupName);
    }

    public boolean contains(String command) {
        return generalGroupBlacklist.isListed(command);
    }

    public boolean contains(String command, String server) {
        return generalGroupBlacklist.isListed(command) || getOrCreateGroupBlacklist(server).isListed(command);
    }

    public boolean containsOnServer(String command, String server) {
        return getOrCreateGroupBlacklist(server).isListed(command);
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server) {
        GroupBlacklist groupBlacklist;
        if(groupServerBlacklist.containsKey(server)) groupBlacklist = groupServerBlacklist.get(server);
        else {
            groupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
            groupServerBlacklist.put(server, groupBlacklist);
        }

        groupBlacklist.load();
        return groupBlacklist;
    }

    public List<String> getCommands() {
        return generalGroupBlacklist.getCommands();
    }

    public List<String> getCommands(String server) {
        List<String> commands = new ArrayList<>(generalGroupBlacklist.getCommands());
        commands.addAll(getOrCreateGroupBlacklist(server).getCommands());
        return commands;
    }

    public void deleteGroup() {
        deleteGroup(null);
    }

    public void deleteGroup(String server) {
        final String path = "groups." + groupName + (server != null ? "." + server : "");
        //Storage.Files.STORAGE.setAndSave(path + ".commands", null);
        Storage.Files.STORAGE.setAndSave(path, null);
        Storage.Files.STORAGE.reload();
    }

    public ConfigurationBuilder getConfig() {
        return generalGroupBlacklist.getConfig();
    }

    public void save() {
        generalGroupBlacklist.save();
    }
}
