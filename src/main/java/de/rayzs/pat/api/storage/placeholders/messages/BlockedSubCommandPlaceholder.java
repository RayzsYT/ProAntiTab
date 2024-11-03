package de.rayzs.pat.api.storage.placeholders.messages;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class BlockedSubCommandPlaceholder extends PlaceholderStorage {

    public BlockedSubCommandPlaceholder() { super("message_sub_blocked"); }

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.buildStringList(Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE.getLines());
    }
}
