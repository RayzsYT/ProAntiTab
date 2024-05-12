package de.rayzs.pat.plugin.listeners.bukkit;

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

        event.getCommands().removeIf(command -> {
           if(Storage.TURN_BLACKLIST_TO_WHITELIST)
               return !Storage.isCommandBlockedPrecise(command) && !PermissionUtil.hasBypassPermission(player, command);
           else return Storage.isCommandBlocked(command) && !PermissionUtil.hasBypassPermission(player, command);
        });
    }

    public static void updateCommands() {
        if(Reflection.getMinor() >= 18) Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
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
