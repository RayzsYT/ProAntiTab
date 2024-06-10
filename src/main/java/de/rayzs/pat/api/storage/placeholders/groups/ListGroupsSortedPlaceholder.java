package de.rayzs.pat.api.storage.placeholders.groups;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.group.*;

public class ListGroupsSortedPlaceholder extends PlaceholderStorage {

    public ListGroupsSortedPlaceholder() { super("list_sorted_groups"); }

    public String COMMAND, SPLITTER;

    @Override
    public String onRequest(String param) {
        Group group = GroupManager.getGroupByName(param);
        if(group == null) return null;

        return StringUtils.buildSortedStringList(
                GroupManager.getGroupNames(),
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
        COMMAND = new ConfigSectionHelper<String>(this, "group", "&f%group%\\n").getOrSet();
    }
}
