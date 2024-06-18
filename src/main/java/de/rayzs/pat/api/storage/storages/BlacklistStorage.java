package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.api.storage.*;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.permission.PermissionUtil;

import java.io.Serializable;
import java.util.*;

public class BlacklistStorage extends StorageTemplate implements Serializable {

    private List<String> commands = new ArrayList<>();

    public BlacklistStorage(String navigatePath) {
        super(Storage.Files.STORAGE, navigatePath);
    }

    public boolean isListed(String command) {
        return isListed(command, false);
    }

    public boolean isConverted(String command, boolean intensive) {
        return !command.contains(" ") && (intensive && !command.contains(":"));
    }

    public String convertCommand(String command, boolean intensive, boolean lowerCase) {
        return convertCommand(command, intensive, lowerCase, true);
    }

    public String convertCommand(String command, boolean intensive, boolean lowerCase, boolean slash) {
        if(isConverted(command, intensive)) return command;
        if(lowerCase) command = command.toLowerCase();

        if(slash)
            if(command.startsWith("/")) command = command.replaceFirst("/", "");

        String[] split;
        if(command.contains(" ")) {
            split = command.split(" ");
            if(split.length > 0) command = split[0];
        }

        if(intensive && command.contains(":")) {
            split = command.split(":");
            if(split.length > 0)
                command = command.replaceFirst(split[0] + ":", "");
        }

        return command;
    }

    public boolean isListed(String command, boolean intensive) {
        if(commands == null) {
            Logger.warning("&cFailed to check command \"" + command + "\"! PAT couldn't read the commands from the storage.yml at section &4" + getNavigatePath() + "&c! Please ensure that your storage.yml isn't corrupt or contains any spaces or empty commands. If this problem still remains, please join my Discord server to ask for help there. &8(&erayzs.de/discord&8)");
            return false;
        }

        if(!isConverted(command, intensive)) command = convertCommand(command, intensive, true);

        for (String commands : commands) {
            if(commands == null) continue;
            if (commands.equals(command)) return true;
        }

        return false;
    }

    public boolean isListed(String command, boolean intensive, boolean convert) {
        if(convert)
            if(!isConverted(command, intensive)) command = convertCommand(command, intensive, true);

        for (String commands : commands)
            if(commands.equals(command)) return true;

        return false;
    }

    public boolean isBlocked(Object targetObj, String command) {
        return isBlocked(targetObj, command, false);
    }

    public boolean isBlocked(Object targetObj, String command, boolean intensive) {
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return !isListed(command, intensive) && !PermissionUtil.hasBypassPermission(targetObj, command);
        else
            return isListed(command, intensive) && !PermissionUtil.hasBypassPermission(targetObj, command);
    }

    public boolean isBlockedIgnorePermission(String command, boolean intensive) {
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return !isListed(command, intensive);
        else
            return isListed(command, intensive);
    }

    public boolean isBlocked(Object targetObj, String command, boolean intensive, boolean convert) {
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return !isListed(command, intensive, convert) && !PermissionUtil.hasBypassPermission(targetObj, command);
        else
            return isListed(command, intensive, convert) && !PermissionUtil.hasBypassPermission(targetObj, command);
    }

    public boolean isBlocked(String command, boolean intensive) {
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return !isListed(command, intensive);
        else
            return isListed(command, intensive);
    }

    public boolean isBlocked(String command, boolean intensive, boolean convert) {
        if(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED)
            return !isListed(command, intensive, convert);
        else
            return isListed(command, intensive, convert);
    }

    public void setList(List<String> commands) {
        this.commands = commands;
    }

    public BlacklistStorage add(String command) {
        command = command.toLowerCase();
        if(!commands.contains(command))
            commands.add(command);
        return this;
    }

    public BlacklistStorage remove(String command) {
        command = command.toLowerCase();
        commands.remove(command);
        return this;
    }

    public BlacklistStorage clear() {
        commands.clear();
        return this;
    }

    public List<String> getCommands() {
        return commands;
    }

    @Override
    public void save() {
        getConfig().setAndSave(getNavigatePath(), commands);
    }

    @Override
    public void load() {
        getConfig().reload();
        commands = (ArrayList<String>) getConfig().getOrSet(getNavigatePath(), commands);
    }
}
