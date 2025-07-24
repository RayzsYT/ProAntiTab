package de.rayzs.pat.utils.adapter;

import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.plugin.listeners.bungee.WaterfallAntiTabListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import net.luckperms.api.event.sync.PreNetworkSyncEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.VelocityLoader;
import net.luckperms.api.model.user.User;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BukkitLoader;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.*;
import de.rayzs.pat.utils.Reflection;
import net.luckperms.api.node.*;
import net.luckperms.api.*;
import java.util.*;

public class LuckPermsAdapter {

    private static LuckPerms PROVIDER;

    public static void initialize() {
        Logger.info("Successfully hooked into LuckPerms for easier usage.");

        Storage.USE_LUCKPERMS = true;
        PROVIDER = LuckPermsProvider.get();
        EventBus eventBus = PROVIDER.getEventBus();

        eventBus.subscribe(Storage.PLUGIN_OBJECT, NodeMutateEvent.class, LuckPermsAdapter::onNoteMutate);

        if (!Reflection.isProxyServer()) {

            if (BukkitLoader.useSuggestions())
                eventBus.subscribe(Storage.PLUGIN_OBJECT, PreNetworkSyncEvent.class, event -> BukkitAntiTabListener.luckpermsNetworkSync());

        } else {

            if (Reflection.isVelocityServer()) {
                eventBus.subscribe(Storage.PLUGIN_OBJECT, PreNetworkSyncEvent.class, event -> VelocityLoader.delayedPermissionsReload());
            }

            Storage.getLoader().updateCommandCache();

        }
    }


    public static User getUser(UUID uuid) {
        return PROVIDER.getUserManager().getUser(uuid);
    }

    public static Map<String, Boolean> getPermissions(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return null;

        return user.getCachedData().getPermissionData().getPermissionMap();
    }

    public static boolean hasAnyPermissions(UUID uuid) {
        Map<String, Boolean> permissions = getPermissions(uuid);
        if (permissions == null) return false;

        return !permissions.isEmpty();
    }

    public static void setPermissions(UUID uuid) {
        Map<String, Boolean> permissions = getPermissions(uuid);
        if (permissions == null) return;


        permissions.forEach((permission, permitted) -> {

            if (permission.startsWith("proantitab.") || permission.equals("*"))
                PermissionUtil.setPermission(uuid, permission, permitted);

        });

        if (Reflection.isProxyServer()) {
            Storage.getLoader().updateCommandCache();
            return;
        }

        if (Reflection.getMinor() >= 13)
            BukkitAntiTabListener.handleTabCompletion(uuid);
    }

    private static void onNoteMutate(NodeMutateEvent event) {
        boolean relevant = false, inheritance = false;

        if (event.isUser())
            for (Node node : event.getDataAfter()) {

            if (!inheritance)
                inheritance = node.getType() == NodeType.INHERITANCE;

            if (node.getType() != NodeType.PERMISSION && (node.getKey().startsWith("proantitab.") || !node.getKey().equals("*"))) {
                relevant = true;
                break;
            }
        }

        if (!relevant && !inheritance)
            return;

        if (event.isUser() && event.getTarget() instanceof User) {
            User user = (User) event.getTarget();

            if (!Reflection.isProxyServer()) {

                if (Reflection.getMinor() >= 13)
                    BukkitAntiTabListener.luckpermsNetworkUserSync(user.getUniqueId());

                return;
            }

            if (Reflection.isVelocityServer())
                PermissionUtil.reloadPermissions(user.getUniqueId());
        }
    }
}
