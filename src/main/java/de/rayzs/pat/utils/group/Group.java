package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.utils.permission.PermissionUtil;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Group implements Serializable {

    private final HashMap<String, GroupBlacklist> groupServerBlacklist = new HashMap<>();
    private final GroupBlacklist generalGroupBlacklist;
    private final ExpireCache<String, List<GroupBlacklist>> cachedServerGroupBlacklists = new ExpireCache<>(1, TimeUnit.HOURS);
    private final String groupName;
    private int priority;

    public Group(String groupName) {
        this.groupName = groupName;
        this.priority = (int) Storage.Files.STORAGE.getOrSet("groups." + groupName + ".priority", 1);
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.load();
    }

    public Group(String groupName, int priority) {
        this.groupName = groupName;
        this.priority = priority;
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.load();
    }

    public Group(String groupName, List<String> commands) {
        this.groupName = groupName;
        this.priority = (int) Storage.Files.STORAGE.getOrSet("groups." + groupName + ".priority", 1);
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.setList(commands);
    }

    public Group(String groupName, int priority, List<String> commands) {
        this.groupName = groupName;
        this.priority = priority;
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
        GroupBlacklist serverGroupBlacklist = getOrCreateGroupBlacklist(server, true);
        serverGroupBlacklist.load();
        serverGroupBlacklist.add(command).save();
    }

    public void remove(String command) {
        this.generalGroupBlacklist.remove(command).save();
    }

    public void remove(String command, String server) {
        GroupBlacklist serverGroupBlacklist = getOrCreateGroupBlacklist(server, true);
        serverGroupBlacklist.load();
        serverGroupBlacklist.remove(command).save();
    }

    public void clear() {
        this.generalGroupBlacklist.clear().save();
    }

    public void clear(String server) {
        GroupBlacklist serverGroupBlacklist = getOrCreateGroupBlacklist(server, true);
        serverGroupBlacklist.clear().save();
        serverGroupBlacklist.load();
        Storage.Files.STORAGE.reload();
    }

    public void setCommands(List<String> commands) {
        this.generalGroupBlacklist.setList(commands);
    }

    public void setPriority(int priority) {
        Storage.Files.STORAGE.setAndSave("groups." + groupName + ".priority", priority);
        this.priority = priority;
    }

    public boolean hasPermission(Object targetObj) {
        return PermissionUtil.hasPermission(targetObj, "group." + this.groupName);
    }

    public boolean contains(String command) {
        return this.generalGroupBlacklist.isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
    }

    public boolean contains(String command, boolean intensive) {
        return this.generalGroupBlacklist.isListed(command, intensive);
    }

    public boolean contains(String command, boolean intensive, boolean convert) {
        return this.generalGroupBlacklist.isListed(command, intensive, convert);
    }

    public boolean contains(String command, boolean intensive, boolean convert, boolean slash) {
        return this.generalGroupBlacklist.isListed(command, intensive, convert, slash);
    }

    public boolean contains(String command, String server) {
        GroupBlacklist groupBlacklist = getOrCreateGroupBlacklist(server);
        return (this.generalGroupBlacklist.isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) || groupBlacklist != null && getOrCreateGroupBlacklist(server).isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED));
    }

    public boolean containsOnServer(String command, String server) {
        GroupBlacklist serverGroupBlacklist = getOrCreateGroupBlacklist(server);
        return (serverGroupBlacklist != null && serverGroupBlacklist.isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED));
    }

    public boolean containsOnServer(String command, String server, boolean convert) {
        GroupBlacklist serverGroupBlacklist = getOrCreateGroupBlacklist(server);
        return (serverGroupBlacklist != null && serverGroupBlacklist.isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED, convert));
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server) {
        return getOrCreateGroupBlacklist(server, false);
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server, boolean ignoreExist) {
        GroupBlacklist groupBlacklist;
        server = server.toLowerCase();
        if (this.groupServerBlacklist.containsKey(server) && this.groupServerBlacklist.get(server) != null)
            groupBlacklist = this.groupServerBlacklist.get(server);
        else {
            groupBlacklist = BlacklistCreator.createGroupBlacklist(this.groupName, server, ignoreExist);
            this.groupServerBlacklist.put(server, groupBlacklist);
        }
        if (groupBlacklist != null)
            groupBlacklist.load();
        return groupBlacklist;
    }

    public int getPriority() {
        return priority;
    }

    public void clearServerGroupBlacklistsCache() {
        cachedServerGroupBlacklists.clear();
    }

    public List<GroupBlacklist> getAllServerGroupBlacklist(String server) {
        return getAllServerGroupBlacklist(server, false);
    }

    /*
    CHECK IF GROUP COMMANDS PER SERVER WORK OR NOT
     */

    public List<GroupBlacklist> getAllServerGroupBlacklist(String server, boolean useDefault) {
        server = server.toLowerCase();

        if (cachedServerGroupBlacklists.contains(server)) {
            if(cachedServerGroupBlacklists.get(server).size() > (useDefault ? 1 : 0))
                return cachedServerGroupBlacklists.get(server);
        }

        List<GroupBlacklist> groupBlacklists = new ArrayList<>();
        if(useDefault) groupBlacklists.add(generalGroupBlacklist);

        GroupBlacklist groupBlacklist;

        for (String key : groupServerBlacklist.keySet()) {
             if(!Storage.isServer(key, server)) continue;
             groupBlacklist = groupServerBlacklist.get(key);

             if(groupBlacklist == null) continue;
             groupBlacklists.add(groupBlacklist);
        }

        return cachedServerGroupBlacklists.putAndGet(server, groupBlacklists);
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

    public List<String> getAllCommands(String server) {
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
