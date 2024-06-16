package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.utils.adapter.ViaVersionAdapter;
import de.rayzs.pat.utils.permission.PermissionUtil;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitAntiTabListener implements Listener {

    private static final CommandsCache COMMANDS_CACHE = new CommandsCache();

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if(Storage.USE_VELOCITY || !Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && player.isOp()) return;

        if(!BukkitLoader.isLoaded()) {
            event.getCommands().clear();
            return;
        }

        if(event.getCommands().size() == 0) return;
        List<String> allCommands = getCommands();

        COMMANDS_CACHE.handleCommands(allCommands);

        if (PermissionUtil.hasBypassPermission(player)) return;

        if(Storage.USE_VIAVERSION)
            if(Reflection.getMinor() >= 16 && ViaVersionAdapter.getPlayerProtocol(uuid) < 754)
                event.getCommands().clear();

        final List<String> playerCommands = COMMANDS_CACHE.getPlayerCommands(event.getCommands(), player, player.getUniqueId());
        event.getCommands().clear();
        event.getCommands().addAll(playerCommands);
    }

    public static void updateCommands(Player player) {
        if(notUpdatablePlayer(player.getUniqueId())) return;
        if(Reflection.getMinor() >= 16) player.updateCommands();
    }

    public static void updateCommands() {
        if(Reflection.getMinor() >= 16) Bukkit.getOnlinePlayers().forEach(BukkitAntiTabListener::updateCommands);
    }

    public static void handleTabCompletion(List<String> commands) {
        if(Storage.USE_VELOCITY) return;
        COMMANDS_CACHE.reset();
        Bukkit.getOnlinePlayers().forEach(player -> handleTabCompletion(player, commands));
    }

    public static void handleTabCompletion() {
        COMMANDS_CACHE.reset();
        Bukkit.getOnlinePlayers().forEach(BukkitAntiTabListener::handleTabCompletion);
    }

    public static void handleTabCompletion(Player player) {
        handleTabCompletion(player, getCommands());
    }

    public static List<String> getCommands() {
        return Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED
                ? Storage.Blacklist.getBlacklist().getCommands() : BukkitLoader.getAllCommands();
    }

    public static void handleTabCompletion(UUID uuid) {
        if(notUpdatablePlayer(uuid)) return;

        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        handleTabCompletion(player);
    }

    public static void handleTabCompletion(UUID uuid, List<String> commands) {
        if(notUpdatablePlayer(uuid)) return;

        Player player = Bukkit.getPlayer(uuid);
        if(player == null) return;
        handleTabCompletion(player, commands);
    }

    public static void luckpermsNetworkSync() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BukkitLoader.getPlugin(), PermissionUtil::reloadPermissions, 40);
    }

    public static void luckpermsNetworkUserSync(UUID uuid) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(BukkitLoader.getPlugin(), () -> PermissionUtil.reloadPermissions(uuid));
    }

    public static void handleTabCompletion(Player player, List<String> commands) {
        if(Storage.USE_VELOCITY) return;
        if(notUpdatablePlayer(player.getUniqueId())) return;

        List<String> dummy = new ArrayList<>(commands);
        PlayerCommandSendEvent event = new PlayerCommandSendEvent(player, dummy);
        Bukkit.getPluginManager().callEvent(event);
        updateCommands(player);
    }

    private static boolean notUpdatablePlayer(UUID uuid) {
        if(Storage.USE_VIAVERSION)
            return Reflection.getMinor() >= 16 && ViaVersionAdapter.getPlayerProtocol(uuid) < 754;

        return false;
    }

}
