package de.rayzs.pat.api.storage.placeholders.commands.general;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class ListReversedCommandsPlaceholder extends PlaceholderStorage {

    public ListReversedCommandsPlaceholder() { super("list_reversed_commands"); }

    public String SPLITTER;

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.getReversedStringList(Storage.Blacklist.getBlacklist().getCommands(), SPLITTER);
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7, &e").getOrSet();
    }
}
