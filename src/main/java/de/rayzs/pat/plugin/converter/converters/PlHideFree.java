package de.rayzs.pat.plugin.converter.converters;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class PlHideFree extends Converter {

    public PlHideFree() {
        super("PlHide", "Pl-Hide-Free", "config");
    }

    @Override
    public void apply(CommandSender sender) {
        List<String> groups = config.getKeys("groups", false).stream().toList();

        for (String groupName : groups) {
            List<String> execution = (ArrayList<String>) config.get("groups." + groupName + ".commands");
            List<String> commands = execution.stream().filter(s -> !s.startsWith("!") && !s.equalsIgnoreCase("*")).map(this::translate).toList();

            BlacklistStorage storage;

            if (groupName.equalsIgnoreCase("default")) {
                storage = Storage.Blacklist.getBlacklist();
            } else {
                Group group = GroupManager.registerAndGetGroup(groupName);
                storage = group.getGeneralGroupBlacklist();
            }

            applyStorage(storage, commands);

        }

        sender.sendMessage("§e§lNotice: §7Since PAT is mainly focussed on either blocking or whitelisting commands entirely, PAT is viewing and converting the file such that commands are only working in whitelist mode. If you wish to use ProAntiTab in blacklist mode instead, then please checkout the wiki! (https://github.com/RayzsYT/ProAntiTab/wiki/How-to#introduction)");
    }

    private String translate(String input) {
        if (input.contains(" ")) {
            if (input.endsWith("~")) {
                return translate(input.substring(0, input.length() - 1) + "_-");
            }

            if (input.endsWith("*")) {
                return input.substring(0, input.length() - 2);
            } else if (input.contains("*")) {
                return input.substring(0, input.indexOf('*') - 1);
            }
        }

        return input;
    }
}
