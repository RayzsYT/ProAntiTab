package de.rayzs.pat.api.storage.placeholders.general;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import org.bukkit.entity.Player;

public class GeneralNewestVersionPlaceholder extends PlaceholderStorage {

    public GeneralNewestVersionPlaceholder() {
        super("general_version_newest");
    }

    @Override
    public String onRequest(Player player, String param) {
        return Storage.NEWER_VERSION;
    }
}
