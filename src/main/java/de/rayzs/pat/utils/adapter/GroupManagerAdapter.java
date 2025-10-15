package de.rayzs.pat.utils.adapter;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.scheduler.PATScheduler;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.events.GMGroupEvent;
import org.anjocaido.groupmanager.events.GMSystemEvent;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class GroupManagerAdapter implements Listener {

    private static GroupManager manager;

    public static void initialize(Plugin plugin) {
        Logger.info("Successfully hooked into GroupManager for easier usage.");

        Storage.setPermissionPlugin(PermissionPlugin.GROUPMANAGER);
        manager = (GroupManager) plugin;

        Bukkit.getServer().getPluginManager().registerEvents(new GroupManagerListener(), BukkitLoader.getPlugin());
    }

    public static void setPermissions(UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return;
        }

        final AnjoPermissionsHandler handler = manager.getWorldsHolder().getWorldPermissions(player);
        handler.getAllPlayersPermissions(player.getName()).stream()
                .filter(permission -> permission.startsWith("proantitab."))
                .forEach(permission -> PermissionUtil.setPermission(uuid, permission, true));

        if (Reflection.getMinor() >= 13) {
            BukkitAntiTabListener.handleTabCompletion(uuid);
        }
    }

    private static class GroupManagerListener implements Listener {

        @EventHandler
        public void onGMUser(GMUserEvent event) {
            PATScheduler.createScheduler(() -> {
                PermissionUtil.reloadPermissions(event.getUser().getBukkitPlayer().getUniqueId());
            }, 10);
        }

        @EventHandler
        public void onGMGroup(GMGroupEvent event) {
            PATScheduler.createScheduler(PermissionUtil::reloadPermissions, 10);
        }

        @EventHandler
        public void onGMSystem(GMSystemEvent event) {
            PATScheduler.createScheduler(PermissionUtil::reloadPermissions, 10);
        }
    }
}
