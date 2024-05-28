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
        this.generalGroupBlacklist.add(command).save();
    }

    public void add(String command, String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(this.groupName, server, true);
        serverGroupBlacklist.load();
        serverGroupBlacklist.add(command).save();
    }

    public void remove(String command) {
        this.generalGroupBlacklist.remove(command).save();
    }

    public void remove(String command, String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(this.groupName, server, true);
        serverGroupBlacklist.load();
        serverGroupBlacklist.remove(command).save();
    }

    public void clear() {
        this.generalGroupBlacklist.clear().save();
    }

    public void clear(String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(this.groupName, server, true);
        serverGroupBlacklist.clear().save();
        serverGroupBlacklist.load();
        Storage.Files.STORAGE.reload();
    }

    public void setCommands(List<String> commands) {
        this.generalGroupBlacklist.setList(commands);
    }

    public boolean hasPermission(Object targetObj) {
        return (PermissionUtil.hasPermission(targetObj, "group." + this.groupName) || PermissionUtil.hasServerPermission(targetObj, "group." + this.groupName));
    }

    public boolean contains(String command) {
        return this.generalGroupBlacklist.isListed(command);
    }

    public boolean contains(String command, String server) {
        server = server.toLowerCase();
        return (this.generalGroupBlacklist.isListed(command) || getOrCreateGroupBlacklist(server).isListed(command));
    }

    public boolean containsOnServer(String command, String server) {
        server = server.toLowerCase();
        GroupBlacklist serverGroupBlacklist = getOrCreateGroupBlacklist(server);
        return (serverGroupBlacklist != null && serverGroupBlacklist.isListed(command));
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server) {
        return getOrCreateGroupBlacklist(server, false);
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server, boolean ignoreExist) {
        GroupBlacklist groupBlacklist;
        server = server.toLowerCase();
        if (this.groupServerBlacklist.containsKey(server)) {
            groupBlacklist = this.groupServerBlacklist.get(server);
        } else {
            groupBlacklist = BlacklistCreator.createGroupBlacklist(this.groupName, server, ignoreExist);
            this.groupServerBlacklist.put(server, groupBlacklist);
        }
        if (groupBlacklist != null)
            groupBlacklist.load();
        return groupBlacklist;
    }

    public List<String> getCommands() {
        return this.generalGroupBlacklist.getCommands();
    }

    public List<String> getCommands(String server) {
        server = server.toLowerCase();
        List<String> commands = new ArrayList<>(this.generalGroupBlacklist.getCommands());
        GroupBlacklist groupBlacklist = getOrCreateGroupBlacklist(server);
        if (groupBlacklist != null)
            commands.addAll(groupBlacklist.getCommands());
        return commands;
    }

    public void deleteGroup() {
        deleteGroup(null);
    }

    public void deleteGroup(String server) {
        server = (server != null) ? server.toLowerCase() : null;
        String path = "groups." + this.groupName + ((server != null) ? ("." + server) : "");
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
