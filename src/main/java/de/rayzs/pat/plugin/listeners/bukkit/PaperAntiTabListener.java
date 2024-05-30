package de.rayzs.pat.plugin.listeners.bukkit;

import com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.entity.Player;
import org.bukkit.event.*;

public class PaperAntiTabListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerSendSuggestions(AsyncPlayerSendSuggestionsEvent event) {
        Player player = event.getPlayer();
        if (PermissionUtil.hasBypassPermission(player)) return;

        if(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !BukkitLoader.isLoaded()) event.getSuggestions().getList().clear();
        else event.getSuggestions().getList().removeIf(command -> Storage.Blacklist.isBlocked(player, command.getText(), !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED));
    }
}