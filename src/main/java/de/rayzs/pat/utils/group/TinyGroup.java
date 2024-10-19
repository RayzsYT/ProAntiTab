package de.rayzs.pat.utils.group;

import java.io.Serializable;
import java.util.List;

public class TinyGroup implements Serializable {

    private final String groupName;
    private final int priority;
    private final List<String> commands;

    public TinyGroup(String groupName, int priority, List<String> commands) {
        this.groupName = groupName;
        this.commands = commands;
        this.priority = priority;
    }

    public void add(String command) {
        if (this.commands.contains(command)) return;
        this.commands.add(command);
    }

    public void addAll(List<String> commands) {
        commands.forEach(this::add);
    }

    public String getGroupName() {
        return groupName;
    }

    public int getPriority() {
        return priority;
    }

    public List<String> getCommands() {
        return commands;
    }
}
