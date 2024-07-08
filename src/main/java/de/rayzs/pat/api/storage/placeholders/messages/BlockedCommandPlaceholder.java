package de.rayzs.pat.api.storage.placeholders.messages;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class BlockedCommandPlaceholder extends PlaceholderStorage {

    public BlockedCommandPlaceholder() { super("message_blocked"); }

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.buildStringList(Storage.ConfigSections.Settings.CANCEL_COMMAND.MESSAGE.getLines());
    }
}
