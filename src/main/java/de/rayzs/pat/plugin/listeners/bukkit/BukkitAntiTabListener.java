package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.adapter.ViaVersionAdapter;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitAntiTabListener implements Listener {

    private static final CommandsCache COMMANDS_CACHE = new CommandsCache();
    private static final List<UUID> knownOperators = new ArrayList<>();

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        final boolean noPermissionsPlugin = !Storage.USE_LUCKPERMS && !Storage.USE_GROUPMANAGER;


        if (Storage.USE_VELOCITY) {
            return;
        }

        if (player.isOp()) {

            if (noPermissionsPlugin && !knownOperators.contains(uuid)) {
                knownOperators.add(uuid);
            }

            return;
        }

        if (!BukkitLoader.isLoaded()) {
            event.getCommands().clear();
            return;
        }

        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            return;

        if (event.getCommands().isEmpty()) {
            return;
        }

        List<String> allCommands = getCommands();
        COMMANDS_CACHE.handleCommands(allCommands);

        if (PermissionUtil.hasBypassPermission(player)) {

            if (noPermissionsPlugin && knownOperators.contains(uuid)) {
                PermissionUtil.reloadPermissions(uuid);
                knownOperators.remove(uuid);
            } else {
                return;
            }

        }

        final List<String> playerCommands = COMMANDS_CACHE.getPlayerCommands(new ArrayList<>(event.getCommands()), player, player.getUniqueId());
        FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.callFilteredSuggestionEvents(player, playerCommands);

        event.getCommands().clear();

        if (filteredSuggestionEvent.isCancelled()) {
            return;
        }

        event.getCommands().addAll(filteredSuggestionEvent.getSuggestions());

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.ENABLED) {
            event.getCommands().addAll(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.COMMANDS.getLines());
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.ENABLED) {
            event.getCommands().addAll(Storage.ConfigSections.Settings.CUSTOM_VERSION.COMMANDS.getLines());
        }
    }

    public static List<String> getCommands() {
        return Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED
                ? Storage.Blacklist.getBlacklist().getCommands() : BukkitLoader.getAllCommands();
    }

    private static boolean notUpdatablePlayer(UUID uuid) {
        return Storage.USE_VIAVERSION && Reflection.getMinor() >= 16 && ViaVersionAdapter.getPlayerProtocol(uuid) < 754;
    }


    // --------------- LUCKPERMS SECTIONS --------------- //

    public static void updateCommands() {
        Bukkit.getOnlinePlayers().forEach(BukkitAntiTabListener::updateCommands);
    }

    public static void updateCommands(Player player) {
        if (Reflection.getMinor() >= 13 && !notUpdatablePlayer(player.getUniqueId()))
            player.updateCommands();
    }


    // --------------- UPDATE SECTIONS --------------- //

    public static void handleTabCompletion() {
        COMMANDS_CACHE.reset();
        Bukkit.getOnlinePlayers().forEach(BukkitAntiTabListener::handleTabCompletion);
    }

    public static void handleTabCompletion(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        handleTabCompletion(player);
    }

    public static void handleTabCompletion(Player player) {
        handleTabCompletion(player, getCommands());
    }

    public static void handleTabCompletion(Player player, List<String> commands) {
        if (notUpdatablePlayer(player.getUniqueId()))
            return;

        List<String> dummy = new ArrayList<>(commands);
        PlayerCommandSendEvent event = new PlayerCommandSendEvent(player, dummy);
        Bukkit.getPluginManager().callEvent(event);
        updateCommands(player);
    }

}
