package de.rayzs.pat.api.storage.storages;

import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.api.storage.*;

import java.io.Serializable;
import java.util.*;

public class BlacklistStorage extends StorageTemplate implements Serializable {

    private List<String> commands = new ArrayList<>();
    private Set<String> hiddenCommands = new HashSet<>();

    public BlacklistStorage(String navigatePath) {
        super(Storage.Files.STORAGE, navigatePath);
    }

    public boolean isListed(String command) {
        final boolean caseSensitive = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;
        final boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;

        if (commands == null || command.isEmpty()) {
            return false;
        }

        final boolean isNegated = Storage.Blacklist.BlockTypeFetcher.isNegated(command);
        final boolean takeFirstArgument = !isNegated == turn;

        if (takeFirstArgument) {
            command = StringUtils.getFirstArg(command);
        }

        if (!caseSensitive) {
            command = command.toLowerCase();
        }

        return hiddenCommands.contains(command);
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

    @Override
    public void save() {
        getConfig().setAndSave(getNavigatePath(), commands);
    }

    @Override
    public void load() {
        getConfig().reload();

        final boolean caseSensitive = Storage.ConfigSections.Settings.BASE_COMMAND_CASE_SENSITIVE.ENABLED;


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

                pluginCommand = Storage.Blacklist.BlockType.NEGATE + pluginCommand;
                tmpCommands.add(caseSensitive ? pluginCommand : pluginCommand.toLowerCase());
            }
        }

        for (String pluginListCommand : pluginListCommands) {
            pluginListCommand = pluginListCommand.substring(pluginCommandPrefix.length());

            List<String> pluginCommands = Storage.getLoader().getPluginCommands(pluginListCommand, false);

            for (String pluginCommand : pluginCommands) {
                final String negated = Storage.Blacklist.BlockType.NEGATE + pluginCommand;

                if (!commands.contains(negated) && !tmpCommands.contains(negated)) {
                    tmpCommands.add(caseSensitive ? pluginCommand : pluginCommand.toLowerCase());
                }
            }
        }


        if (!Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) {
            hiddenCommands = new HashSet<>(tmpCommands);
            return;
        }

        final List<String> finalisedCommands = new ArrayList<>();
        for (String command : tmpCommands) {

            if (Storage.Blacklist.BlockTypeFetcher.isNegated(command)) {
                finalisedCommands.add(caseSensitive ? command : StringUtils.lowercaseFirstArgument(command));
                continue;
            }

            if (command.contains(" ")) {
                final String firstArgument = StringUtils.getFirstArg(command);

                if (!finalisedCommands.contains(firstArgument)) {
                    finalisedCommands.add(caseSensitive ? firstArgument : firstArgument.toLowerCase());
                }
            }

            finalisedCommands.add(caseSensitive ? command : StringUtils.lowercaseFirstArgument(command));
        }

        hiddenCommands = new HashSet<>(finalisedCommands);
    }
}
