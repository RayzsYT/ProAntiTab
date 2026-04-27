package de.rayzs.pat.plugin.listeners.bukkit;

import de.rayzs.pat.api.event.events.FilteredSuggestionEvent;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.event.player.PlayerCommandSendEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.hooks.ViaVersionHook;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import org.bukkit.entity.Player;
import de.rayzs.pat.utils.*;
import org.bukkit.event.*;
import org.bukkit.Bukkit;
import java.util.*;

public class BukkitAntiTabListener implements Listener {

    private final List<UUID> knownOperators = new ArrayList<>();


    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerCommandSend(PlayerCommandSendEvent event) {
        final Player player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);
        final UUID uuid = player.getUniqueId();


        if (Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return;
        }

        if (player.isOp()) {
            if (!knownOperators.contains(uuid)) {
                knownOperators.add(uuid);
            }

            return;
        }

        if (event.getCommands().isEmpty()) {
            return;
        }

        Storage.getLoader().getBukkitCommandsCacheMap().handleCommands(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED
                ? Storage.Blacklist.getBlacklist().getCommands()
                : BukkitLoader.getAllCommands()
        );

        if (!player.isOp() && knownOperators.contains(uuid)) {
            knownOperators.remove(uuid);
            PermissionUtil.reloadPermissions(sender);
        }

        if (PermissionUtil.hasBypassPermission(sender)) {
            return;
        }

        if (Reflection.isCraftbukkit() && !Bukkit.getOnlinePlayers().contains(player)) {
            event.getCommands().clear();

            PATScheduler.createScheduler(player::updateCommands, 10);
            return;
        }

        final List<Group> groups = GroupManager.getPlayerGroups(sender);
        final List<String> playerCommands = Storage.getLoader().getBukkitCommandsCacheMap().getPlayerCommands(new ArrayList<>(event.getCommands()), sender, groups);
        final FilteredSuggestionEvent filteredSuggestionEvent = PATEventHandler.callFilteredSuggestionEvents(sender, playerCommands);

        event.getCommands().clear();

        if (filteredSuggestionEvent.isCancelled()) {
            return;
        }

        event.getCommands().addAll(filteredSuggestionEvent.getSuggestions());

        if (Storage.ConfigSections.Settings.CUSTOM_PLUGIN.ALWAYS_TAB_COMPLETABLE) {
            event.getCommands().addAll(Storage.ConfigSections.Settings.CUSTOM_PLUGIN.COMMANDS.getLines());
        }

        if (Storage.ConfigSections.Settings.CUSTOM_VERSION.ALWAYS_TAB_COMPLETABLE) {
            event.getCommands().addAll(Storage.ConfigSections.Settings.CUSTOM_VERSION.COMMANDS.getLines());
        }
    }

    private static boolean notUpdatablePlayer(UUID uuid) {
        return Storage.USE_VIAVERSION && Reflection.isAtLeast(1, 16) && ViaVersionHook.getPlayerProtocol(uuid) < 754;
    }

    public void updateCommands(Player player) {
        if (!notUpdatablePlayer(player.getUniqueId()))
            player.updateCommands();
    }
}
