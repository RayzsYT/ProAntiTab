package de.rayzs.pat.plugin.converter.converters;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.BlacklistStorage;
import de.rayzs.pat.plugin.converter.Converter;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class AdvancedPlHideConverter extends Converter {

    public AdvancedPlHideConverter() {
        super("AdvancedPlHide", "AdvancedPlHide", "config");
    }

    @Override
    public void apply(CommandSender sender) {
        List<String> groups = config.getKeys("groups", false).stream().toList();

        for (String groupName : groups) {
            List<String> commands = (ArrayList<String>) config.get("groups."+ groupName + ".tabcomplete");
            BlacklistStorage storage;

            if (groupName.equalsIgnoreCase("default")) {
                storage = Storage.Blacklist.getBlacklist();
            } else {
                Group group = GroupManager.registerAndGetGroup(groupName);
                storage = group.getGeneralGroupBlacklist();
            }

            applyStorage(storage, commands);

        }

        sender.sendMessage("&e&lNotice: &7AdvanedPlHide works based on permissions whether to whitelist or blacklist commands from a group. Due to the fact that this cannot be detected by PAT and formatted in the exact way, all commands have been interpreted as whitelisted commands instead. Also, since PAT groups are only intended to whitelist commands, all created groups based on AdvancedPlHide have been created with the intention of whitelisting commands instead. If you wish to change that, please check out the GitHub wiki on how to use the storage.yml of PAT the intended way. (https://github.com/RayzsYT/ProAntiTab/wiki/How-to#introduction)");
    }
}
