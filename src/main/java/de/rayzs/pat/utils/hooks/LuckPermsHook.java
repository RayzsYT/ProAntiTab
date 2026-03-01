package de.rayzs.pat.utils.hooks;

import de.rayzs.pat.plugin.system.communication.Communicator;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.sender.CommandSender;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.event.sync.PreNetworkSyncEvent;
import de.rayzs.pat.utils.permission.PermissionUtil;
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
                NodeMutateEvent.class,
                LuckPermsHook::onNoteMutate
        );

        eventBus.subscribe(
                Storage.getLoader().getPluginObj(),
                PreNetworkSyncEvent.class,
                event -> Storage.getLoader().delayedPermissionsReload()
        );
    }


    public static User getUser(UUID uuid) {

        if (uuid == null) {
            Logger.warning("Attempted to get LuckPerms user with null UUID!");
            return null;
        }

        return PROVIDER.getUserManager().getUser(uuid);
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
            final User user = (User) event.getTarget();
            final UUID uuid = user.getUniqueId();

            final CommandSender sender = CommandSender.from(uuid);
            final String serverName = sender.getServerName();

            if (Reflection.isProxyServer()) {
                final List<String> serverCommands = Storage.Blacklist.Collector.collectAllServerCommands(serverName);
                final List<String> groupCommands = Storage.Blacklist.Collector.collectAllPlayerGroupCommands(sender, serverName);

                final List<String> playerCommands = new ArrayList<>(serverCommands);
                playerCommands.addAll(groupCommands);

                SubArgument.get().getUpdateArgumentsHandler().updatePlayerArguments(sender, playerCommands, serverCommands, groupCommands);

                Communicator.Proxy2Backend.sendUpdateCommand(uuid, serverName);
            }

            Storage.getLoader().delayedPermissionsReload();
        }
    }
}
