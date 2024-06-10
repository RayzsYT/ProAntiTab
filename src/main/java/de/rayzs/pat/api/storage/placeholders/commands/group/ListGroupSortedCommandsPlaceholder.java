package de.rayzs.pat.api.storage.placeholders.commands.group;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;

public class ListGroupSortedCommandsPlaceholder extends PlaceholderStorage {

    public ListGroupSortedCommandsPlaceholder() { super("pat_list_sorted_commands_group_"); }

    public String COMMAND, SPLITTER;

    @Override
    public String onRequest(String param) {
        Group group = GroupManager.getGroupByName(param);
        if(group == null) return null;

        return StringUtils.buildSortedStringList(
                group.getCommands(),
                SPLITTER,
                COMMAND,
                "%command%",
                false
        );
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7-> ").getOrSet();
        COMMAND = new ConfigSectionHelper<String>(this, "command", "&f%command%\\n").getOrSet();
    }
}
