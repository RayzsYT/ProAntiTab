package de.rayzs.pat.utils.group;

import de.rayzs.pat.plugin.listeners.BukkitAntiTabListener;
import de.rayzs.pat.utils.*;
import java.util.*;

public class Group {

    private final String groupName;
    private final List<String> commands;

    public Group(String groupName) {
        this.groupName = groupName.toLowerCase();
        this.commands = (List<String>) Storage.STORAGE.getOrSet(this.groupName, new ArrayList<>());
    }

    public Group(String groupName, List<String> commands) {
        this.groupName = groupName.toLowerCase();
        this.commands = commands;
    }

    public void add(String command) {
        command = command.toLowerCase();
        if(contains(command)) return;
        commands.add(command);
        save();

        if(Storage.BLOCKED_COMMANDS_LIST.contains(command)) {
            Storage.BLOCKED_COMMANDS_LIST.add(command);
            Storage.save();
        }

        if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
    }

    public void remove(String command) {
        command = command.toLowerCase();
        if(!contains(command)) return;
        commands.remove(command);
        save();

        if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
    }

    public void clear() {
        commands.clear();
        save();

        if(!Reflection.isBungeecordServer()) BukkitAntiTabListener.updateCommands();
    }

    public boolean hasPermission(Object targetObj) {
        return PermissionUtil.hasPermission(targetObj, "proantitab.bypass.group." + groupName);
    }

    public void save() {
        Storage.STORAGE.setAndSave(groupName, commands);
    }

    public boolean contains(String command) {
        return commands.contains(command);
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getGroupName() {
        return groupName;
    }
}
