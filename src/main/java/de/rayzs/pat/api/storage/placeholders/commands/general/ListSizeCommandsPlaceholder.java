package de.rayzs.pat.api.storage.placeholders.commands.general;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.api.storage.Storage;
import org.bukkit.entity.Player;

public class ListSizeCommandsPlaceholder extends PlaceholderStorage {

    public ListSizeCommandsPlaceholder() { super("list_size_commands"); }

    @Override
    public String onRequest(Player player, String param) {
        return String.valueOf(Storage.Blacklist.getBlacklist().getCommands().size());
    }

    @Override
    public void load() {
        super.load();
    }
}
