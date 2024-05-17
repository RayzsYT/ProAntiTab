package de.rayzs.pat.utils.group;

import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.utils.*;
import java.util.*;

public class Group {

    private final String groupName;
    private List<String> commands;

    public Group(String groupName) {
        this.groupName = groupName.toLowerCase();
        this.commands = !Reflection.isProxyServer() && Storage.BUNGEECORD ? new ArrayList<>() : (List<String>) Storage.STORAGE.getOrSet(this.groupName, new ArrayList<>());
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

        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public void remove(String command) {
        command = command.toLowerCase();
        if(!contains(command)) return;
        commands.remove(command);
        save();

        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public void clear() {
        commands.clear();
        save();

        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public boolean hasPermission(Object targetObj) {
        return PermissionUtil.hasPermission(targetObj, "group." + groupName) || PermissionUtil.hasServerPermission(targetObj, "group." + groupName, Storage.SERVER_NAME);
    }

    public void save() {
        if(!Reflection.isProxyServer() && Storage.BUNGEECORD) return;
        Storage.STORAGE.setAndSave(groupName, commands);
    }

    public boolean contains(String command) {
        String[] split;
        if(command.contains(" ")) {
            split = command.split(" ");
            if(split.length > 0) command = split[0];
            command = command.split(" ")[0];
        }
        return commands.contains(command);
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public String getGroupName() {
        return groupName;
    }
}
