package de.rayzs.pat.utils.hooks;

import de.rayzs.pat.plugin.system.communication.Communicator;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.sender.CommandSender;
import net.luckperms.api.context.ImmutableContextSet;
import de.rayzs.pat.utils.permission.PermissionUtil;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.*;
import de.rayzs.pat.utils.Reflection;
import net.luckperms.api.node.*;
import net.luckperms.api.*;
import net.luckperms.api.query.QueryOptions;

import java.util.*;

public class LuckPermsHook {

    private static LuckPerms PROVIDER;

    public static void initialize() {
        Logger.info("Successfully hooked into LuckPerms for easier usage.");

        Storage.setPermissionPlugin(PermissionPlugin.LUCKPERMS);
        PROVIDER = LuckPermsProvider.get();
        EventBus eventBus = PROVIDER.getEventBus();

        eventBus.subscribe(
                Storage.getLoader().getPluginObj(),
                NodeAddEvent.class,
                event ->
                        handleNodeChange(event.getTarget(), event.getNode())
        );

        eventBus.subscribe(
                Storage.getLoader().getPluginObj(),
                NodeRemoveEvent.class,
                event ->
                        handleNodeChange(event.getTarget(), event.getNode())
        );

        eventBus.subscribe(
                Storage.getLoader().getPluginObj(),
                NodeClearEvent.class,
                event ->
                        handleNodesChange(event.getTarget(), event.getNodes())
        );

/*      Probably not required anymore, since
        commands and permissions are already reloaded, in case
        the player updates some kind of PAT related permission.

        This here only reload permissions and even updates player commands
        for all unnecessarily since why updating EVERYTHING for just
        one tiny group change or anything, which may not even be related
        to any PAT changes?

        At least, so the thought. Therefore to be on the save side, it's disabled for now.
        This should also save some server memory.

        eventBus.subscribe(
                Storage.getLoader().getPluginObj(),
                PostNetworkSyncEvent.class,
                event -> PermissionUtil.reloadPermissions()
        );
 */
    }

    public static Map<String, Boolean> getPermissions(UUID uuid) {
        final User user = PROVIDER.getUserManager().getUser(uuid);

        if (user == null) {
            return null;
        }

        final ImmutableContextSet.Builder builder = ImmutableContextSet.builder();

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED || Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_WORLD.ENABLED) {
            final String contextValue = Storage.getLoader().getPlayerServerName(uuid);

            if (contextValue != null) {
                builder.add("server", contextValue).add("world", contextValue);
            }
        }

        return user.getCachedData().getPermissionData(QueryOptions.contextual(builder.build())).getPermissionMap();
    }

    public static void setPermissions(UUID uuid) {
        Map<String, Boolean> permissions = getPermissions(uuid);
        if (permissions == null) return;

        permissions.forEach((permission, permitted) -> {

            if (permission.startsWith("proantitab.") || permission.equals("*"))
                PermissionUtil.setPermission(uuid, permission, permitted);

        });

        if (Reflection.isProxyServer()) {
            Storage.getLoader().resetCommandsCache();
        } else {
            Storage.getLoader().updateCommands();
        }
    }

    private static void handleNodesChange(PermissionHolder holder, Set<Node> nodes) {
        for (Node node : nodes) {
            if (node.getType() != NodeType.PERMISSION) {
                continue;
            }

            if (isRelevantPermission(node)) {
                handleAfterNodeChange(holder);
                break;
            }
        }
    }

    private static void handleNodeChange(PermissionHolder holder, Node node) {
        if (node.getType() != NodeType.PERMISSION && node.getType() != NodeType.INHERITANCE) {
            return;
        }

        if (!isRelevantPermission(node)) {
            return;
        }

        handleAfterNodeChange(holder);
    }

    private static void handleAfterNodeChange(PermissionHolder holder) {
        if (! (holder instanceof User user)) {

            if (holder instanceof Group group) {
                Storage.getLoader().getPlayerIds().forEach(id -> {
                    final User user = PROVIDER.getUserManager().getUser(id);

                    if (user != null) {
                        final boolean result = user.getCachedData().getPermissionData().checkPermission("group." + group.getName()).asBoolean();
                        if (result) handleAfterNodeChange(user);
                    }
                });
            }

            return;
        }

        final CommandSender sender = CommandSender.from(user.getUniqueId());
        if (sender == null) {
            return;
        }


        final String serverName = sender.getServerName();


        PermissionUtil.reloadPermissions(sender);

        if (Reflection.isProxyServer()) {
            final List<String> serverCommands = Storage.Blacklist.Collector.collectAllServerCommands(serverName);
            final List<String> groupCommands = Storage.Blacklist.Collector.collectAllPlayerGroupCommands(sender, serverName);

            final List<String> playerCommands = new ArrayList<>(serverCommands);
            playerCommands.addAll(groupCommands);

            SubArgument.get().getUpdateArgumentsHandler().updatePlayerArguments(sender, playerCommands, serverCommands, groupCommands);
            Communicator.Proxy2Backend.sendUpdateCommand(user.getUniqueId(), serverName);
        }
    }

    private static boolean isRelevantPermission(Node node) {
        final String key = node.getKey();

        return key.startsWith("group.")
                || key.startsWith("proantitab.")
                || key.equals("*");
    }
}
