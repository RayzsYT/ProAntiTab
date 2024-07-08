package de.rayzs.pat.utils.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.jetbrains.annotations.*;
import org.bukkit.entity.Player;

public class PlaceholderHook extends PlaceholderExpansion {

    public PlaceholderHook() {
        Storage.USE_PLACEHOLDERAPI = true;
        Storage.ConfigSections.Placeholders.initialize();
        Logger.info("Successfully hooked into PlaceholderAPI!");
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pat";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return BukkitLoader.getPlugin().getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return BukkitLoader.getPlugin().getDescription().getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String request) {
        return Storage.ConfigSections.Placeholders.findAndReplace(player, request.toLowerCase());
    }
}
