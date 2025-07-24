package de.rayzs.pat.api.storage.placeholders.groups;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.*;
import org.bukkit.entity.Player;

public class ListGroupsReversedPlaceholder extends PlaceholderStorage {

    public ListGroupsReversedPlaceholder() { super("list_reversed_groups"); }

    public String SPLITTER;

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.getReversedStringList(GroupManager.getGroupNames(), SPLITTER);
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7, &e").getOrSet();
    }
}
