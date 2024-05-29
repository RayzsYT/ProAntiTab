package de.rayzs.pat.utils.luckperms;

import de.rayzs.pat.api.communication.ClientCommunication;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.luckperms.api.model.user.User;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.*;
import net.luckperms.api.node.*;
import net.luckperms.api.*;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class LuckPermsAdapter {

    private static LuckPerms PROVIDER;

    public static void initialize() {
        Logger.info("Successfully hooked into LuckPerms for easier usage.");

        Storage.USE_LUCKPERMS = true;
        PROVIDER = LuckPermsProvider.get();
        EventBus eventBus = PROVIDER.getEventBus();

        /*
        eventBus.subscribe(Storage.PLUGIN_OBJECT, NodeAddEvent.class, LuckPermsAdapter::onNodeAdd);
        eventBus.subscribe(Storage.PLUGIN_OBJECT, NodeRemoveEvent.class, LuckPermsAdapter::onNodeRemove);
         */
    }

    public static void setPermissions(UUID uuid) {
        User user = PROVIDER.getUserManager().getUser(uuid);
        if(user == null) return;

        user.getCachedData().getPermissionData().getPermissionMap().forEach((permission, permitted) -> {
                    if(permission.startsWith("proantitab.")) PermissionUtil.setPermission(uuid, permission, permitted);
                }
        );

        if(Reflection.getMinor() >= 18) BukkitAntiTabListener.handleTabCompletion(Storage.Blacklist.getBlacklist().getCommands());
    }

    private static void onNodeAdd(NodeAddEvent event) {
        if (!event.isUser()) return;
        Node node = event.getNode();

        if (node.getType() != NodeType.INHERITANCE
                || !(event.getTarget() instanceof User)
                || !node.getKey().startsWith("proantitab."))
            return;

        User user = (User) event.getTarget();
        PermissionUtil.resetPermissions(user.getUniqueId());
    }

    private static void onNodeRemove(NodeRemoveEvent event) {
        if (!event.isUser()) return;
        Node node = event.getNode();

        if (node.getType() != NodeType.INHERITANCE
                || !(event.getTarget() instanceof User)
                || !node.getKey().startsWith("proantitab."))
            return;

        User user = (User) event.getTarget();
        PermissionUtil.resetPermissions(user.getUniqueId());
    }
}
