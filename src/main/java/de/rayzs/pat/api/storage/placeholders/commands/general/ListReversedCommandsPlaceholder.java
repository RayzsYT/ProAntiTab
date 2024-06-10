package de.rayzs.pat.api.storage.placeholders.commands.general;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;

public class ListReversedCommandsPlaceholder extends PlaceholderStorage {

    public ListReversedCommandsPlaceholder() { super("list_reversed_commands"); }

    public String COMMAND, SPLITTER;

    @Override
    public String onRequest(String param) {
        return StringUtils.buildSortedStringList(
                Storage.Blacklist.getBlacklist().getCommands(),
                SPLITTER,
                COMMAND,
                "%command%",
                true
        );
    }

    @Override
    public void load() {
        super.load();
        SPLITTER = new ConfigSectionHelper<String>(this, "splitter", "&7-> ").getOrSet();
        COMMAND = new ConfigSectionHelper<String>(this, "command", "&f%command%\\n").getOrSet();
    }
}
