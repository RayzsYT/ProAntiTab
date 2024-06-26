package de.rayzs.pat.api.storage.placeholders.general;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class GeneralCurrentVersionPlaceholder extends PlaceholderStorage {

    public GeneralCurrentVersionPlaceholder() { super("general_version_current"); }

    @Override
    public String onRequest(Player player, String param) {
        return Storage.CURRENT_VERSION;
    }
}
