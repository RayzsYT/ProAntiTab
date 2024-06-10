package de.rayzs.pat.api.storage.placeholders.commands.group;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import org.bukkit.entity.Player;

public class ListGroupSizeCommandsPlaceholder extends PlaceholderStorage {

    public ListGroupSizeCommandsPlaceholder() { super("list_size_commands_group_"); }

    @Override
    public String onRequest(Player player, String param) {
        Group group = GroupManager.getGroupByName(param);
        if(group == null) return null;
        return String.valueOf(group.getCommands().size());
    }

    @Override
    public void load() {
        super.load();
    }
}
