package de.rayzs.pat.plugin.process.impl.local.modify;

import de.rayzs.pat.api.command.ProCommand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.plugin.converter.StorageConverter;
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
            return false;
        }

        String pluginName = args[0];
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
            sender.sendMessage("&cUsage: /pat extract [plugin] <group> <colons>");
            return true;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (plugin == null) {
            sender.sendMessage("&cPlugin '" + pluginName + "' not found.");
            return true;
        }

        List<String> commands = new ArrayList<>();
        for (Map.Entry<String, Command> entry : BukkitLoader.getCommandsMap().entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(plugin.getName().toLowerCase() + ":")) {
                String command = entry.getKey().substring(args[0].length() + 1);
                commands.add(command);

                if (useColons) {
                    commands.add(entry.getKey());
                }
            }
        }

        BlacklistStorage storage = group == null
                ? Storage.Blacklist.getBlacklist()
                : group.getGeneralGroupBlacklist();

        commands = commands.stream().filter(command -> !storage.isListed(command)).toList();
        commands.forEach(storage::add);

        storage.save();

        sender.sendMessage("&aDone! Extracted &e" + commands.size() + "&a commands in total.");
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        final int length = args.length;

        if (length <= 1) {
            return Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).toList();
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
