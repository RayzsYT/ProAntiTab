package de.rayzs.pat.api.storage.placeholders.groups;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import org.bukkit.entity.Player;

public class ListGroupsPlaceholder extends PlaceholderStorage {

    public String COMMAND, SPLITTER;

    public ListGroupsPlaceholder() {
        super("list_groups");
    }

    @Override
    public String onRequest(Player player, String param) {
        Group group = GroupManager.getGroupByName(param);
        if (group == null) return null;

        return StringUtils.buildStringList(
                GroupManager.getGroupNames(),
                SPLITTER,
                COMMAND,
                "%group%"
        );
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7-> ").getOrSet();
        COMMAND = new ConfigSectionHelper<String>(this, "group", "&f%group%\\n").getOrSet();
    }
}
