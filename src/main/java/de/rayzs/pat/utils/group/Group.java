package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.utils.PermissionUtil;

import java.io.Serializable;
import java.util.*;

public class Group implements Serializable {

    private final HashMap<String, GroupBlacklist> serverGroupBlacklist = new HashMap<>();

    private final GroupBlacklist generalGroupBlacklist;
    private final String groupName;

    public Group(String groupName) {
        this.groupName = groupName;
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
    }

    public String getGroupName() {
        return groupName;
    }

    public void add(String command) {
        generalGroupBlacklist.add(command).save();
    }

    public void add(String command, String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
        serverGroupBlacklist.add(command).save();
    }

    public void remove(String command) {
        generalGroupBlacklist.remove(command).save();
    }

    public void remove(String command, String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
        serverGroupBlacklist.remove(command).save();
    }

    public void clear() {
        generalGroupBlacklist.clear().save();
    }

    public Set<String> getServerNames() {
        return serverGroupBlacklist.keySet();
    }

    public void clear(String server) {
        GroupBlacklist serverGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
        serverGroupBlacklist.clear().save();
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

    public List<String> getCommands() {
        return generalGroupBlacklist.getCommands();
    }

    public List<String> getCommands(String server) {
        List<String> commands = new ArrayList<>(generalGroupBlacklist.getCommands());
        commands.addAll(getOrCreateGroupBlacklist(server).getCommands());
        return commands;
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server) {
        GroupBlacklist groupBlacklist = null;
        if(!serverGroupBlacklist.containsKey(server)) {
            groupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
            serverGroupBlacklist.put(server, groupBlacklist);
        }

        return serverGroupBlacklist.getOrDefault(server, groupBlacklist);
    }

    public void deleteGroup() {
        deleteGroup(null);
    }

    public void deleteGroup(String server) {
        GroupBlacklist groupBlacklist = server == null ? generalGroupBlacklist : serverGroupBlacklist.get(server);
        if(groupBlacklist == null) return;
        groupBlacklist.getConfig().setAndSave("groups." + groupName + (server != null ? "." + server : ""), null);
    }

    public ConfigurationBuilder getConfig() {
        return generalGroupBlacklist.getConfig();
    }

    public void save() {
        generalGroupBlacklist.save();
    }
}
