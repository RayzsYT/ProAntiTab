package de.rayzs.pat.api.storage.placeholders.messages;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class UnknownCommandPlaceholder extends PlaceholderStorage {

    public UnknownCommandPlaceholder() { super("message_unknowncommand"); }

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.buildStringList(Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE.getLines());
    }
}
