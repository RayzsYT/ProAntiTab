package de.rayzs.pat.utils.luckperms;

import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.luckperms.api.model.user.User;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.*;
import net.luckperms.api.node.*;
import net.luckperms.api.*;

public class LuckPermsAdapter {

    public static void initialize() {
        Logger.info("You're using LuckPerms? Hope you don't mind if I hook myself into it. ^^");

        LuckPerms provider = LuckPermsProvider.get();
        EventBus eventBus = provider.getEventBus();

        eventBus.subscribe(provider, NodeAddEvent.class, LuckPermsAdapter::onNodeAdd);
        eventBus.subscribe(provider, NodeRemoveEvent.class, LuckPermsAdapter::onNodeRemove);
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
