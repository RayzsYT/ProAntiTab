package de.rayzs.pat.utils.group;

import java.io.Serializable;
import java.util.List;

public class TinyGroup implements Serializable {

    private final String groupName;
    private final List<String> commands;

    public TinyGroup(String groupName, List<String> commands) {
        this.groupName = groupName;
        this.commands = commands;
    }

    public String getGroupName() {
        return groupName;
    }

    public List<String> getCommands() {
        return commands;
    }
}
