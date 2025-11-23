package de.rayzs.pat.plugin.converter.converters;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlHidePro extends Converter {

    public PlHidePro() {
        super("PlHidePro", "Pl-Hide-Pro", "config");
    }

    @Override
    public void apply(CommandSender sender) {

        boolean updateGroupPerWorld = (boolean) config.get("per_world_group_updates");
        Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_WORLD.getConfig().setAndSave("update-groups-per-world", updateGroupPerWorld);

        boolean autoLowercaseCommands = (boolean) config.get("auto_lowercase_base_commands");
        Storage.ConfigSections.Settings.AUTO_LOWERCASE_COMMANDS.getConfig().setAndSave("auto-lowercase-commands.enabled", autoLowercaseCommands);

        boolean f3brand = (boolean) config.get("replace-f3-server-brand");
        String f3brandString = (String) config.get("f3-server-brand");
        Storage.ConfigSections.Settings.CUSTOM_BRAND.getConfig().setAndSave("custom-server-brand.enabled", f3brand);

        if (f3brand) {
            Storage.ConfigSections.Settings.CUSTOM_BRAND.getConfig().setAndSave("custom-server-brand.repeat-delay", -1);
            Storage.ConfigSections.Settings.CUSTOM_BRAND.getConfig().setAndSave("custom-server-brand.brands", new ArrayList<>(Collections.singletonList(f3brandString.replace("§", "&"))));
        }

        List<String> groups = config.getKeys("groups", false).stream().toList();

        for (String groupName : groups) {
            List<String> commands = new ArrayList<>();

            final List<String> execution = (ArrayList<String>) config.get("groups." + groupName + ".commands");
            final List<String> tabCompletion = (ArrayList<String>) config.get("groups." + groupName + ".tabcomplete");

            for (String s : execution) {
                if (s.equalsIgnoreCase("*")) {
                    tabCompletion.remove("*");
                    continue;
                }

                if (!s.contains(" ") && s.startsWith("!")) {
                    tabCompletion.remove(s);
                    continue;
                }

                if (tabCompletion.contains(s)) {
                    commands.add(s);
                    tabCompletion.remove(s);
                    continue;
                }

                commands.add("[CMD]" + s);
            }

            commands.addAll(tabCompletion.stream().map(s -> "[TAB]" + s).toList());
            commands = commands.stream().map(this::translate).toList();

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
