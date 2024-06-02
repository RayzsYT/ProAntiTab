package de.rayzs.pat.utils.adapter;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.luckperms.api.model.user.User;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.*;
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
        if(permissions == null) return false;

        return permissions.size() > 0;
    }

    public static void setPermissions(UUID uuid) {
        Map<String, Boolean> permissions = getPermissions(uuid);
        if(permissions == null) return;


        permissions.forEach((permission, permitted) -> {
                    if(permission.startsWith("proantitab.") || permission.equals("*")) PermissionUtil.setPermission(uuid, permission, permitted);
                }
        );

        if(Reflection.getMinor() >= 18) BukkitAntiTabListener.handleTabCompletion(uuid, Storage.Blacklist.getBlacklist().getCommands());
    }

    private static void onNoteMutate(NodeMutateEvent event) {
        if (!event.isUser()) return;
        boolean relevant = false;
        for(Node node : event.getDataAfter()) {
            if (node.getType() != NodeType.INHERITANCE
                    || !(event.getTarget() instanceof User)
                    || !node.getKey().startsWith("proantitab.")
                    || !node.getKey().equals("*"))
                continue;
            relevant = true;
            break;
        }

        if(!relevant) return;

        User user = (User) event.getTarget();
        PermissionUtil.reloadPermissions(user.getUniqueId());
    }
}