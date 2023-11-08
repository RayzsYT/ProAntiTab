package de.rayzs.pat.plugin.listeners;

import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;

public class BukkitAntiTabListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if(PermissionUtil.hasBypassPermission(player)) return;

        for (int i = 0; i < event.getCommands().size(); i++) {
            String command = (String) event.getCommands().toArray()[i], tempName = command;

            if (tempName.contains(":")) tempName = tempName.split(":")[1];

            if (Storage.isCommandBlocked(tempName)) {
                if(PermissionUtil.hasBypassPermission(player, tempName)) {
                    event.getCommands().add(command);
                    if(!event.getCommands().contains(tempName)) event.getCommands().add(tempName);
                }
                else {
                    event.getCommands().remove(command);
                    if(event.getCommands().contains(tempName)) event.getCommands().remove(tempName);
                }
            }
        }
    }

    public static void updateCommands() {
        if(Reflection.getMinor() >= 18) Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }
}
