package de.rayzs.pat.api.storage.placeholders.commands.group;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.*;
import org.bukkit.entity.Player;

public class ListGroupSortedCommandsPlaceholder extends PlaceholderStorage {

    public ListGroupSortedCommandsPlaceholder() { super("list_sorted_commands_group_"); }

    public String SPLITTER;

    @Override
    public String onRequest(Player player, String param) {
        Group group = GroupManager.getGroupByName(param);
        if(group == null) return null;

        return StringUtils.getSortedStringList(group.getCommands(), SPLITTER);
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7, &e").getOrSet();
    }
}
