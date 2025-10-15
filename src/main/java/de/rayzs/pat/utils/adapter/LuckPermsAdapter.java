package de.rayzs.pat.utils.adapter;

import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import net.luckperms.api.event.sync.PreNetworkSyncEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.VelocityLoader;
import net.luckperms.api.model.user.User;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
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

        Storage.setPermissionPlugin(PermissionPlugin.LUCKPERMS);
        PROVIDER = LuckPermsProvider.get();
        EventBus eventBus = PROVIDER.getEventBus();

        eventBus.subscribe(Storage.PLUGIN_OBJECT, NodeMutateEvent.class, LuckPermsAdapter::onNoteMutate);
        eventBus.subscribe(Storage.PLUGIN_OBJECT, PreNetworkSyncEvent.class, event -> Storage.getLoader().delayedPermissionsReload());
    }


    public static User getUser(UUID uuid) {
        return PROVIDER.getUserManager().getUser(uuid);
    }

    public static Map<String, Boolean> getPermissions(UUID uuid) {
        User user = getUser(uuid);
        if (user == null) return null;

        return user.getCachedData().getPermissionData().getPermissionMap();
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
            UUID uuid = user.getUniqueId();

            PermissionUtil.reloadPermissions(user.getUniqueId());

            if (Reflection.isProxyServer()) {
                Communicator.sendUpdateCommand(uuid);

                if (Reflection.isVelocityServer()) {
                    VelocityLoader.delayedPlayerReload(uuid);
                }

            }

        }
    }
}
