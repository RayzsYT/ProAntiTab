package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.storage.*;

import java.io.Serializable;
import java.util.*;

public class BlacklistStorage extends StorageTemplate implements Serializable {

    private List<String> commands = new ArrayList<>();
    private List<String> hiddenCommands = new ArrayList<>();

    public BlacklistStorage(String navigatePath) {
        super(Storage.Files.STORAGE, navigatePath);
    }

    public boolean isListed(String command) {
        return isListed(command, false);
    }

    public boolean isListed(String command, boolean ignoreColons) {
        final boolean caseSensitive = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;

        if (commands == null || command.isEmpty()) {
            return false;
        }

        /*
        if (!isNegated(command) && commands.contains("*")) {
            return true;
        }
        */

        boolean isNegated = isNegated(command);

        if (!isNegated) {
            command = StringUtils.getFirstArg(command);
        }

        for (String listedCommand : hiddenCommands) {
            if (listedCommand == null)
                continue;

            if (ignoreColons) {
                command = StringUtils.getFirstArg(command);

                if (command.contains(":"))
                    command = command.substring(command.indexOf(':'));
            }

            if (!isNegated) {
                listedCommand = StringUtils.getFirstArg(listedCommand);
            }

            if (caseSensitive && listedCommand.equals(command)) {
                return true;
            } else if (!caseSensitive && listedCommand.equalsIgnoreCase(command)) {
                return true;
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

    public List<String> getHiddenCommands() {
        return hiddenCommands;
    }

    @Override
    public void save() {
        getConfig().setAndSave(getNavigatePath(), commands);
    }

    @Override
    public void load() {
        getConfig().reload();

        commands = (ArrayList<String>) getConfig().getOrSet(getNavigatePath(), commands);

        List<String> tmpCommands = new ArrayList<>(commands);
        Set<String> pluginListCommands = new HashSet<>();

        final String pluginCommandPrefix = "plugin=";

        for (String command : tmpCommands) {
            if (command.startsWith(pluginCommandPrefix)) {
                pluginListCommands.add(command);
            }
        }

        tmpCommands.removeAll(pluginListCommands);

        for (String pluginListCommand : pluginListCommands) {
            pluginListCommand = pluginListCommand.substring(pluginCommandPrefix.length());

            List<String> pluginCommands = Storage.getLoader().getPluginCommands(pluginListCommand, false);

            for (String pluginCommand : pluginCommands) {
                if (!commands.contains(Storage.Blacklist.BlockType.NEGATE + pluginCommand)) {
                    tmpCommands.add(pluginCommand);
                }
            }
        }

        hiddenCommands = tmpCommands;
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
