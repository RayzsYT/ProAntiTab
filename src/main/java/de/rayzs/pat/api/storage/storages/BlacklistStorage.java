package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.*;

import java.io.Serializable;
import java.util.*;

public class BlacklistStorage extends StorageTemplate implements Serializable {

    private List<String> commands = new ArrayList<>();

    public BlacklistStorage(String navigatePath) {
        super(Storage.Files.STORAGE, navigatePath);
    }

    public boolean isListed(String command) {
        return isListed(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
    }

    public boolean isListed(String command, boolean ignoreColons) {
        final boolean caseSensitive = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;

        if (commands == null || command.isEmpty()) {
            return false;
        }

        if (!isNegated(command) && commands.contains("*")) {
            return true;
        }

        for (String listedCommand : commands) {
            if (listedCommand == null)
                continue;

            if (ignoreColons) {
                command = StringUtils.getFirstArg(command);

                if (command.contains(":"))
                    command = command.substring(command.indexOf(':'));
            }

            if (caseSensitive) {
                if (StringUtils.getFirstArg(listedCommand).equals(StringUtils.getFirstArg(command))) {
                    return true;
                }
            } else {
                if (StringUtils.getFirstArg(listedCommand).equalsIgnoreCase(StringUtils.getFirstArg(command))) {
                    return true;
                }
            }
        }

        return false;
    }

    public void setList(List<String> commands) {
        this.commands = commands;
    }

    public BlacklistStorage add(String command) {
        if (!commands.contains(command))
            commands.add(command);

        return this;
    }

    public BlacklistStorage remove(String command) {
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

    private boolean isNegated(String command) {
        if (command.isEmpty()) {
            return false;
        }

        boolean negated = command.charAt(0) == '!';

        if (!negated && command.length() > 5)
            negated = command.charAt(5) == '!';

        return negated;
    }
}
