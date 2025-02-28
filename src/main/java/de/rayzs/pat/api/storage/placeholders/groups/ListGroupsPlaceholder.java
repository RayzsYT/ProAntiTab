package de.rayzs.pat.api.storage.placeholders.groups;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.*;
import org.bukkit.entity.Player;

public class ListGroupsPlaceholder extends PlaceholderStorage {

    public ListGroupsPlaceholder() { super("list_groups"); }

    public String COMMAND, SPLITTER;

    @Override
    public String onRequest(Player player, String param) {
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
