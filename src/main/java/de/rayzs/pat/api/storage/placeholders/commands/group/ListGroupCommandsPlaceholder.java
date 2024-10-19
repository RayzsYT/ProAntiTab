package de.rayzs.pat.api.storage.placeholders.commands.group;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import org.bukkit.entity.Player;

public class ListGroupCommandsPlaceholder extends PlaceholderStorage {

    public String COMMAND, SPLITTER;

    public ListGroupCommandsPlaceholder() {
        super("list_commands_group_");
    }

    @Override
    public String onRequest(Player player, String param) {
        Group group = GroupManager.getGroupByName(param);
        if (group == null) return null;

        return StringUtils.buildStringList(
                group.getCommands(),
                SPLITTER,
                COMMAND,
                "%command%"
        );
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7-> ").getOrSet();
        COMMAND = new ConfigSectionHelper<String>(this, "command", "&f%command%\\n").getOrSet();
    }
}
