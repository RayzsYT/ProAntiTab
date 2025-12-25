package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.plugin.converter.StorageConverter;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.*;

public class ExtractCommand extends ProCommand {

    public ExtractCommand() {
        super(
                "extract",
                ""
        );
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(Storage.ConfigSections.Messages.EXTRACT.USAGE);
            return true;
        }

        String pluginName = args[0].toLowerCase();
        boolean failed = false, useColons = false;
        Group group = null;

        if (args.length == 2) {

            group = GroupManager.getGroupByName(args[1]);
            if (group == null) {
                useColons = Boolean.parseBoolean(args[1]);

                if (!useColons && !args[1].equalsIgnoreCase("false")) {
                    failed = true;
                }

            }

            try {
                useColons = Boolean.parseBoolean(args[1]);
            } catch (Exception exception) {
                group = GroupManager.getGroupByName(args[1]);
                failed = group == null;
            }

        } else if (args.length == 3) {
            group = GroupManager.getGroupByName(args[1]);

            if (group == null) {
                failed = true;
            } else {
                try {
                    useColons = Boolean.parseBoolean(args[2]);
                } catch (Exception exception) {
                    failed = true;
                }
            }
        }

        if (failed) {
            sender.sendMessage(Storage.ConfigSections.Messages.EXTRACT.USAGE);
            return true;
        }

        List<String> pluginNames = Storage.getLoader().getPluginNames("%n");
        boolean pluginFound = false, all = pluginName.equalsIgnoreCase("*");

        for (String name : pluginNames) {
            if (name.equalsIgnoreCase(pluginName)) {
                pluginFound = true;
                break;
            }
        }

        if (!all && !pluginFound) {
            sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.EXTRACT.PLUGIN_NOT_FOUND,
                    "%plugin%", pluginName)
            );

            return true;
        }

        List<String> commands = all ? Storage.getLoader().getAllCommands(useColons) : Storage.getLoader().getPluginCommands(args[0], useColons);

        BlacklistStorage storage = group == null
                ? Storage.Blacklist.getBlacklist()
                : group.getGeneralGroupBlacklist();

        commands = commands.stream().filter(command -> !storage.isListed(command)).toList();
        commands.forEach(storage::add);

        storage.save();

        sender.sendMessage(StringUtils.replace(Storage.ConfigSections.Messages.EXTRACT.SUCCESS,
                "%amount%", String.valueOf(commands.size()),
                "%plugin%", pluginName
        ));

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        final int length = args.length;

        if (length <= 1) {
            final List<String> suggestions =  Storage.getLoader().getPluginNames("%n");
            suggestions.add("*");

            return suggestions;
        }

        List<String> result = new ArrayList<>();

        if (length == 2 || length == 3 && GroupManager.getGroupByName(args[1]) != null) {
            result.add("true");
            result.add("false");
        }

        if (length == 2) {
            result.addAll(GroupManager.getGroupNames());
        }

        return result;
    }
}
