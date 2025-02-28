package de.rayzs.pat.api.storage.placeholders.general;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import org.bukkit.entity.Player;

public class GeneralPrefixPlaceholder extends PlaceholderStorage {

    public GeneralPrefixPlaceholder() { super("general_prefix"); }

    @Override
    public String onRequest(Player player, String param) {
        return Storage.ConfigSections.Messages.PREFIX.PREFIX;
    }
}
