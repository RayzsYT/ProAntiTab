package de.rayzs.pat.api.storage.placeholders.groups;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import org.bukkit.entity.Player;

public class ListGroupsReversedPlaceholder extends PlaceholderStorage {

    public String COMMAND, SPLITTER;

    public ListGroupsReversedPlaceholder() {
        super("list_reversed_groups");
    }

    @Override
    public String onRequest(Player player, String param) {
        Group group = GroupManager.getGroupByName(param);
        if (group == null) return null;

        return StringUtils.buildSortedStringList(
                GroupManager.getGroupNames(),
                SPLITTER,
                COMMAND,
                "%group%",
                true
        );
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7-> ").getOrSet();
        COMMAND = new ConfigSectionHelper<String>(this, "group", "&f%group%\\n").getOrSet();
    }
}
