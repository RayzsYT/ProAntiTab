package de.rayzs.pat.api.storage.placeholders.general;

import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.utils.configuration.helper.ConfigSectionHelper;
import org.bukkit.entity.Player;

public class GeneralUserPlaceholder extends PlaceholderStorage {

    public GeneralUserPlaceholder() { super("general_user"); }

    public String CONSOLE;

    @Override
    public String onRequest(Player player, String param) {
        return player == null ? CONSOLE : player.getName();
    }

    @Override
    public void load() {
        super.load();
        CONSOLE = new ConfigSectionHelper<String>(this, "console-name", "Server console").getOrSet();
    }
}
