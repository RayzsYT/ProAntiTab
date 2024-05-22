package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitAntiTabListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (PermissionUtil.hasBypassPermission(player)) return;

        if(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !BukkitLoader.isLoaded()) event.getCommands().clear();
        else event.getCommands().removeIf(command -> Storage.Blacklist.isNotTabable(player, command));
    }

    public static void updateCommands() {
        if(Reflection.getMinor() >= 16) Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }

    public static void handleTabCompletion(List<String> commands) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            List<String> dummy = new ArrayList<>(commands);
            PlayerCommandSendEvent event = new PlayerCommandSendEvent(player, dummy);
            Bukkit.getPluginManager().callEvent(event);
            player.updateCommands();
        }
    }
}
