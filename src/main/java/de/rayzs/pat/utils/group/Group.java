package de.rayzs.pat.utils.group;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.permission.PermissionUtil;

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

        loadServerBlacklist();
    }

    public Group(String groupName, int priority) {
        this.groupName = groupName;
        this.priority = priority;
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.load();

        loadServerBlacklist();
    }

    public Group(String groupName, List<String> commands) {
        this.groupName = groupName;
        this.priority = (int) Storage.Files.STORAGE.getOrSet("groups." + groupName + ".priority", 1);
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.setList(commands);

        loadServerBlacklist();
    }

    public Group(String groupName, int priority, List<String> commands) {
        this.groupName = groupName;
        this.priority = priority;
        this.generalGroupBlacklist = BlacklistCreator.createGroupBlacklist(groupName);
        this.generalGroupBlacklist.setList(commands);

        loadServerBlacklist();
    }

    public String getGroupInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("  groupName: ")
                .append(groupName).append("\n")
                .append("  priority: ")
                .append(priority).append("\n");

        builder.append("  commands: ")
                .append(String.join(", ", getCommands()))
                .append("\n");

        builder.append("  servers:\n");

        for (Map.Entry<String, GroupBlacklist> entry : groupServerBlacklist.entrySet()) {
            builder.append("    ")
                    .append(entry.getKey())
                    .append(": ");

            for (GroupBlacklist blacklist : getAllServerGroupBlacklist(entry.getKey())) {
                builder.append(String.join(", ", blacklist.getCommands()));
                builder.append(", ");
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    private void loadServerBlacklist() {
        String path = "groups." + groupName + ".servers";

        if (Storage.Files.STORAGE.get(path) == null)
            return;

        Storage.Files.STORAGE
                .getKeys("groups." + groupName + ".servers", false)
                .forEach(this::getOrCreateGroupBlacklist);
    }

    public String getGroupName() {
        return groupName;
    }

    public GroupBlacklist getGeneralGroupBlacklist() {
        return generalGroupBlacklist;
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
        return contains(command, null);
    }

    public boolean contains(String command, String server) {
        if (generalGroupBlacklist.isListed(command)) {
            return true;
        }

        if (server == null)
            return false;

        GroupBlacklist groupBlacklist = getOrCreateGroupBlacklist(server);

        if (groupBlacklist != null) {
            return groupBlacklist.isListed(command);
        }

        return false;
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server) {
        return getOrCreateGroupBlacklist(server, false);
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server, boolean ignoreExist) {
        if (server == null)
            return null;

        GroupBlacklist groupBlacklist;

        if (this.groupServerBlacklist.get(server) != null)
            groupBlacklist = this.groupServerBlacklist.get(server);
        else {
            groupBlacklist = BlacklistCreator.createGroupBlacklist(this.groupName, server, ignoreExist);

            if (groupBlacklist != null) {
                groupBlacklist.load();
                this.groupServerBlacklist.put(server, groupBlacklist);
            }
        }

        return groupBlacklist;
    }

    public int getPriority() {
        return priority;
    }

    public void clearServerGroupBlacklistsCache() {
        cachedServerGroupBlacklists.clear();
    }

    public List<String> getBlacklistServerNames(String server) {
        return groupServerBlacklist.keySet().stream().filter(key ->
                Storage.isServer(key, server)
        ).toList();
    }

    public List<GroupBlacklist> getAllServerGroupBlacklist(String server) {
        return getAllServerGroupBlacklist(server, false);
    }

    /*
    CHECK IF GROUP COMMANDS PER SERVER WORK OR NOT
     */

    public List<GroupBlacklist> getAllServerGroupBlacklist(String server, boolean useDefault) {

        if (cachedServerGroupBlacklists.contains(server)) {

            if (cachedServerGroupBlacklists.get(server).size() > (useDefault ? 1 : 0))
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
        List<String> commands = new ArrayList<>();

        GroupBlacklist groupBlacklist = getOrCreateGroupBlacklist(server);
        if (groupBlacklist != null)
            commands.addAll(groupBlacklist.getCommands());

        return commands;
    }

    public List<String> getAllCommands(String server) {
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
