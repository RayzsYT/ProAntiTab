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

        boolean isNegated = Storage.Blacklist.BlockTypeFetcher.isNegated(command);

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
        if (!commands.contains(command)) {
            commands.add(command);
            hiddenCommands.add(command);
        }

        return this;
    }

    public BlacklistStorage remove(String command) {
        commands.remove(command);
        hiddenCommands.remove(command);
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

        final List<String> tmpCommands = commands != null ? new ArrayList<>(commands) : new ArrayList<>();
        final Set<String> pluginListCommands = new HashSet<>(), negatedPluginListCommands = new HashSet<>();

        final String pluginCommandPrefix = "plugin=";
        final String negatedPluginCommandPrefix = Storage.Blacklist.BlockType.NEGATE + pluginCommandPrefix;

        for (String command : tmpCommands) {
            if (command.startsWith(pluginCommandPrefix)) {
                pluginListCommands.add(command);
            }

            if (command.startsWith(negatedPluginCommandPrefix)) {
                negatedPluginListCommands.add(command);
            }
        }

        tmpCommands.removeAll(pluginListCommands);
        tmpCommands.removeAll(negatedPluginListCommands);

        for (String negatedPluginCommand : negatedPluginListCommands) {
            negatedPluginCommand = negatedPluginCommand.substring(negatedPluginCommandPrefix.length());

            List<String> pluginCommands = Storage.getLoader().getPluginCommands(negatedPluginCommand, false);

            for (String pluginCommand : pluginCommands) {
                tmpCommands.remove(pluginCommand);

                tmpCommands.add(Storage.Blacklist.BlockType.NEGATE + pluginCommand);
            }
        }

        for (String pluginListCommand : pluginListCommands) {
            pluginListCommand = pluginListCommand.substring(pluginCommandPrefix.length());

            List<String> pluginCommands = Storage.getLoader().getPluginCommands(pluginListCommand, false);

            for (String pluginCommand : pluginCommands) {
                final String negated = Storage.Blacklist.BlockType.NEGATE + pluginCommand;

                if (!commands.contains(negated) && !tmpCommands.contains(negated)) {
                    tmpCommands.add(pluginCommand);
                }
            }
        }

        hiddenCommands = tmpCommands;
    }
}
