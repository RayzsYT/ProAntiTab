package de.rayzs.pat.api.storage.placeholders.messages;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class BlockedBaseCommandPlaceholder extends PlaceholderStorage {

    public BlockedBaseCommandPlaceholder() { super("message_base_blocked"); }

    @Override
    public String onRequest(Player player, String param) {
        if (Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE == null) return null;
        return StringUtils.getStringList(Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE.getLines(), "\n");
    }
}
