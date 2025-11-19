package de.rayzs.pat.plugin.converter.converters;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.blacklist.impl.GroupBlacklist;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.plugin.converter.StorageConverter;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.ArrayList;
import java.util.List;

public class CommandWhitelist extends Converter {

    public CommandWhitelist() {
        super("CommandWhitelist", "CommandWhitelist", "config");
    }

    @Override
    public void apply() {
        List<String> groups = config.getKeys(true).stream()
                .filter(key -> key.startsWith("groups.") && StringUtils.countMatches('.', key) == 1)
                .toList();

        for (String groupPath : groups) {
            String groupName = groupPath.substring(groupPath.lastIndexOf('.') + 1);
            List<String> commands = new ArrayList<>();

            final List<String> l1 = (ArrayList<String>) config.get(groupPath + ".commands");
            final List<String> l2 = (ArrayList<String>) config.get(groupPath + ".subcommands");

            commands.addAll(l1);
            commands.addAll(l2);

            BlacklistStorage storage;

            if (groupPath.endsWith(".default")) {
                storage = Storage.Blacklist.getBlacklist();
            } else {
                Group group = GroupManager.registerAndGetGroup(groupName);
                storage = group.getGeneralGroupBlacklist();
            }

            applyStorage(storage, commands);
        }
    }
}
