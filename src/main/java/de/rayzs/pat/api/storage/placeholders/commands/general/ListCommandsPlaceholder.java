package de.rayzs.pat.api.storage.placeholders.commands.general;

import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.StringUtils;
import org.bukkit.entity.Player;

public class ListCommandsPlaceholder extends PlaceholderStorage {

    public ListCommandsPlaceholder() { super("list_commands"); }

    public String COMMAND, SPLITTER;

    @Override
    public String onRequest(Player player, String param) {
        return StringUtils.buildStringList(
                Storage.Blacklist.getBlacklist().getCommands(),
                SPLITTER,
                COMMAND,
                "%command%"
        );
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7-> ").getOrSet();
        COMMAND = new ConfigSectionHelper<String>(this, "command", "&f%command%\\n").getOrSet();
    }
}
