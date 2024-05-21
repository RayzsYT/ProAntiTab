package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupServer {

    private final HashMap<String, GroupBlacklist> groupServerBlacklist = new HashMap<>();
    private final String groupName;

    public GroupServer(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getAllServers() {
        return new ArrayList<>(groupServerBlacklist.keySet());
    }

    public GroupBlacklist getOrCreateGroupBlacklist(String server) {
        GroupBlacklist groupBlacklist = null;
        if(!groupServerBlacklist.containsKey(server)) {
            System.out.println("New group: " + groupName);
            groupBlacklist = BlacklistCreator.createGroupBlacklist(groupName, server);
            groupBlacklist.load();
            groupServerBlacklist.put(server, groupBlacklist);
        } else System.out.println("Already existing group: " + groupName);

        return groupServerBlacklist.getOrDefault(server, groupBlacklist);
    }
}
