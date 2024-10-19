package de.rayzs.pat.api.storage.placeholders.groups;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.group.GroupManager;
import org.bukkit.entity.Player;

public class ListSizeGroupsPlaceholder extends PlaceholderStorage {

    public ListSizeGroupsPlaceholder() {
        super("list_size_groups");
    }

    @Override
    public String onRequest(Player player, String param) {
        return String.valueOf(GroupManager.getGroups().size());
    }

    @Override
    public void load() {
        super.load();
    }
}
