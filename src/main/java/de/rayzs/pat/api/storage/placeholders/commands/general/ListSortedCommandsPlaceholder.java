package de.rayzs.pat.api.storage.placeholders.commands.general;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class ListSortedCommandsPlaceholder extends PlaceholderStorage {

    public ListSortedCommandsPlaceholder() { super("list_sorted_commands"); }

    public String COMMAND, SPLITTER;

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.buildSortedStringList(
                Storage.Blacklist.getBlacklist().getCommands(),
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
