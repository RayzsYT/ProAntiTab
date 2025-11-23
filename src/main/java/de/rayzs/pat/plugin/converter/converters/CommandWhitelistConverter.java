package de.rayzs.pat.plugin.converter.converters;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandWhitelistConverter extends Converter {

    public CommandWhitelistConverter() {
        super("CommandWhitelist", "CommandWhitelist", "config");
    }

    @Override
    public void apply(CommandSender sender) {
        List<String> groups = config.getKeys("groups", false).stream().toList();

        for (String groupName : groups) {
            List<String> commands = new ArrayList<>();

            final List<String> l1 = (ArrayList<String>) config.get("groups." + groupName + ".commands");
            final List<String> l2 = (ArrayList<String>) config.get("groups." + groupName + ".subcommands");

            commands.addAll(l1);
            commands.addAll(l2);

            BlacklistStorage storage;

            if (groupName.equalsIgnoreCase("default")) {
                storage = Storage.Blacklist.getBlacklist();
            } else {
                Group group = GroupManager.registerAndGetGroup(groupName);
                storage = group.getGeneralGroupBlacklist();
            }

            applyStorage(storage, commands);
        }
    }
}
