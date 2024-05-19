package de.rayzs.pat.utils.group;

import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.utils.*;
import java.util.*;

public class Group {

    private final String groupName;
    private final GroupBlacklist commands;

    public Group(String groupName) {
        this.groupName = groupName.toLowerCase();
        this.commands = BlacklistCreator.createGroupBlacklist(groupName);
    }

    public Group(String groupName, List<String> commands) {
        this.groupName = groupName.toLowerCase();
        this.commands = BlacklistCreator.createGroupBlacklist(groupName);
        this.commands.setList(commands);
    }

    public void add(String command) {
        command = command.toLowerCase();
        if(contains(command)) return;

        commands.add(command).save();
        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public void remove(String command) {
        command = command.toLowerCase();
        if(!contains(command)) return;

        commands.remove(command).save();
        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public void clear() {
        commands.clear().save();
        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public boolean hasPermission(Object targetObj) {
        return PermissionUtil.hasPermission(targetObj, "group." + groupName) || PermissionUtil.hasServerPermission(targetObj, "group." + groupName);
    }

    public boolean contains(String command) {
        return commands.isListed(command);
    }

    public List<String> getCommands() {
        return commands.getCommands();
    }

    public void setCommands(List<String> commands) {
        this.commands.setList(commands);
        if(!Reflection.isProxyServer() && Reflection.getMinor() >= 18) BukkitAntiTabListener.updateCommands();
    }

    public String getGroupName() {
        return groupName;
    }
}
