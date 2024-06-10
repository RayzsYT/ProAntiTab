package de.rayzs.pat.utils.hooks;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.StringUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;

public class PlaceholderHook extends PlaceholderExpansion {

    public PlaceholderHook() {
        Storage.USE_PLACEHOLDERAPI = true;
        Storage.ConfigSections.Placeholders.initialize();
    }

    @Override
    public boolean persist() {
        return super.persist();
    }

    @Override
    public boolean register() {
        return super.register();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "proantitab";
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
        String result = null;
        request = request.toLowerCase();

        if(request.startsWith("pat_")) {
            request = StringUtils.replaceFirst(request,"pat_", "");
            result = Storage.ConfigSections.Placeholders.findAndReplace(request);
        }

        return result != null ? result : super.onPlaceholderRequest(player, request);
    }
}
