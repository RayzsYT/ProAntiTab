package de.rayzs.pat.plugin.listeners;

import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class BukkitAntiTabListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if(PermissionUtil.hasBypassPermission(player)) return;

        List<String> dummy = new ArrayList<>(event.getCommands()),
                commands = new ArrayList<>();
        for (int i = 0; i < dummy.size(); i++) {
            String command = dummy.get(i), tempName = command;

            if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
                if(!PermissionUtil.hasBypassPermission(player, tempName) && Storage.isCommandBlockedPrecise(tempName))
                    commands.add(command);
                continue;
            }

            if (tempName.contains(":")) tempName = tempName.split(":")[1];

            if (Storage.isCommandBlocked(tempName)) {
                if(PermissionUtil.hasBypassPermission(player, tempName)) continue;
                event.getCommands().remove(command);
                if(event.getCommands().contains(tempName)) event.getCommands().remove(tempName);
            }
        }

        if(Storage.TURN_BLACKLIST_TO_WHITELIST) {
            event.getCommands().clear();
            event.getCommands().addAll(commands);
        }
    }

    public static void updateCommands() {
        if(Reflection.getMinor() >= 18) Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
    }
}
