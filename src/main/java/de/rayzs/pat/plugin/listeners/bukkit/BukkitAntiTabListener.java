package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.utils.adapter.ViaVersionAdapter;
import de.rayzs.pat.utils.permission.PermissionUtil;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitAntiTabListener implements Listener {

    private static List<String> COMMANDS = null;

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !BukkitLoader.isLoaded()) {
            event.getCommands().clear();
            return;
        }

        if(event.getCommands().size() == 0) return;

        if(COMMANDS == null) {
            COMMANDS = new LinkedList<>();

            for (String command : event.getCommands()) {
                if(COMMANDS.contains(command)) continue;

                if (Storage.Blacklist.isBlocked(command, !Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED))
                    continue;

                COMMANDS.add(command);
            }
        }

        if (PermissionUtil.hasBypassPermission(player)) return;

        if(Storage.VELOCITY_SYNC) {
            if(!Storage.USE_VIAVERSION)
                if(Reflection.getMinor() >= 16 && ViaVersionAdapter.getPlayerProtocol(uuid) < 754)
                    event.getCommands().clear();

            return;
        }

        List<String> commands = new LinkedList<>(COMMANDS);
        if (!(Storage.USE_LUCKPERMS && !LuckPermsAdapter.hasAnyPermissions(uuid))) {
            for (String command : event.getCommands()) {
                if (COMMANDS.contains(command)) continue;
                if (!PermissionUtil.hasBypassPermission(player, command)) continue;
                commands.add(command);
            }
        }

        event.getCommands().clear();
        event.getCommands().addAll(commands);
    }

    public static void updateCommands(Player player) {
        if(Storage.VELOCITY_SYNC) return;
        if(Reflection.getMinor() >= 16) player.updateCommands();
    }

    public static void updateCommands() {
        if(Storage.VELOCITY_SYNC) return;
        if(Reflection.getMinor() >= 16) Bukkit.getOnlinePlayers().forEach(BukkitAntiTabListener::updateCommands);
    }

    public static void handleTabCompletion(List<String> commands) {
        if(Storage.VELOCITY_SYNC) return;

        long start = System.currentTimeMillis();
        COMMANDS = null;
        Bukkit.getOnlinePlayers().forEach(player -> handleTabCompletion(player, commands));
        Logger.info("Took " + (System.currentTimeMillis() - start) + "ms to sync commands with players.");
    }

    public static void handleTabCompletion(UUID uuid, List<String> commands) {
        if(Storage.VELOCITY_SYNC) return;
        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        handleTabCompletion(player, commands);
    }


    public static void handleTabCompletion(Player player, List<String> commands) {
        if(Storage.VELOCITY_SYNC) return;
        List<String> dummy = new ArrayList<>(commands);
        PlayerCommandSendEvent event = new PlayerCommandSendEvent(player, dummy);
        Bukkit.getPluginManager().callEvent(event);
        updateCommands(player);
    }
}
