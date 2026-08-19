package de.rayzs.pat.utils.permission;

import java.util.*;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.hooks.GroupManagerHook;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.hooks.LuckPermsHook;

public class PermissionUtil {

    private static final HashMap<UUID, PermissionMap> MAP = new HashMap<>();

    public static void resetPermissions() {
        MAP.forEach((key, value) -> value.clear());
    }

    public static void reloadPermissions() {
        List<UUID> uuids = new ArrayList<>(MAP.keySet());
        uuids.forEach(PermissionUtil::reloadPermissions);
    }

    public static void reloadPermissions(UUID uuid) {
        if (uuid == null) {
            return;
        }

        resetPermissions(uuid);
        setPlayerPermissions(uuid);
    }

    public static void reloadPermissions(CommandSender sender) {
        resetPermissions(sender.getUniqueId());
        setPlayerPermissions(sender);
    }

    public static String getPermissionsAsString(UUID uuid) {

        if (Storage.getPermissionPlugin() == PermissionPlugin.NONE) {
            return "No permission plugin detected. Therefore, no permissions have been cached.";
        }

        PermissionMap permissionMap = MAP.get(uuid);

        if (permissionMap == null) {
            return "";
        }

        return Arrays.toString(permissionMap.getHashedPermissions().toArray()).replace("[", "").replace("]", "");
    }

    public static Set<String> getPermissions(UUID uuid) {
        PermissionMap permissionMap = MAP.get(uuid);
        if(permissionMap == null)
            return null;

        return permissionMap.getHashedPermissions();
    }

    public static void setPlayerPermissions(CommandSender sender) {
        if (Storage.getPermissionPlugin() == PermissionPlugin.LUCKPERMS) {
            LuckPermsHook.setPermissions(sender.getUniqueId());
        } else if (Storage.getPermissionPlugin() == PermissionPlugin.GROUPMANAGER) {
            GroupManagerHook.setPermissions(sender.getUniqueId());
        } else {
            final boolean isOperator = sender.isOperator();
            GroupManager.getGroups().forEach(group -> group.hasPermission(sender, isOperator));
        }
    }

    public static void setPlayerPermissions(UUID uuid) {
        if (Storage.getPermissionPlugin() == PermissionPlugin.LUCKPERMS) {
            LuckPermsHook.setPermissions(uuid);
        } else if (Storage.getPermissionPlugin() == PermissionPlugin.GROUPMANAGER) {
            GroupManagerHook.setPermissions(uuid);
        } else {
            final CommandSender sender = CommandSender.from(uuid);
            if (sender == null) return;

            final boolean isOperator = sender.isOperator();
            GroupManager.getGroups().forEach(group -> group.hasPermission(uuid, isOperator));
        }
    }

    public static void resetPermissions(UUID uuid) {
        if (!MAP.containsKey(uuid))
            return;

        MAP.get(uuid).clear();
    }

    public static void setPermission(UUID uuid, String permission, boolean permitted) {
        PermissionMap permissionMap;

        if(!MAP.containsKey(uuid)) {
            permissionMap = new PermissionMap(uuid);
            MAP.put(uuid, permissionMap);
        } else permissionMap = MAP.get(uuid);

        permissionMap.setState(permission, permitted);
    }

    public static boolean hasPermission(final Object targetObj, final String permission, final boolean isOperator) {
        if (isOperator) return true;


        PermissionMap permissionMap;
        CommandSender sender = null;
        UUID uuid = null;

        if (targetObj instanceof UUID)
            uuid = (UUID) targetObj;
        else if (targetObj instanceof CommandSender)
            sender = (CommandSender) targetObj;
        else
            sender = CommandSender.from(targetObj);

        // No permission plugin found!
        if (Storage.getPermissionPlugin() == PermissionPlugin.NONE) {

            if (sender == null) {
                try {
                    throw new Exception("Unknown sender!");
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

                return true;
            }

            if (sender.isConsole()) {
                return true;
            }

            return sender.hasPermission("*")
                    || sender.hasPermission("proantitab.*")
                    || sender.hasPermission("proantitab." + permission);
        }

        if (uuid == null) {
            if (sender == null || sender.isConsole())
                return true;

            uuid = sender.getUniqueId();
        }

        permissionMap = MAP.get(uuid);

        if (permissionMap == null) {
            permissionMap = new PermissionMap(uuid);
            MAP.put(uuid, permissionMap);
        }

        if (sender != null) {

            if (Storage.getPermissionPlugin() != PermissionPlugin.LUCKPERMS) {

                if (!permissionMap.hasPermissionState("*"))
                    permissionMap.setState("*", sender.hasPermission("*"));

                if (!permissionMap.hasPermissionState("proantitab.*"))
                    permissionMap.setState("proantitab.*", sender.hasPermission("proantitab.*"));

                if (!permissionMap.hasPermissionState("proantitab." + permission))
                    permissionMap.setState("proantitab." + permission, sender.hasPermission("proantitab." + permission));

            }
        }

        return (permissionMap.isPermitted("*") && !Storage.ConfigSections.Settings.IGNORE_STAR_PERMISSION.ENABLED)
                || permissionMap.isPermitted("proantitab.*")
                || permissionMap.isPermitted("proantitab." + permission);
    }

    public static boolean hasBypassPermission(Object targetObj, final boolean isOperator) {

        if (!Reflection.isProxyServer() && Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return false;
        }

        return hasPermission(targetObj, "bypass", isOperator);
    }

    public static boolean hasBypassPermission(
            final Object targetObj,
            final String command,
            final boolean isOperator
    ) {

        if (!Reflection.isProxyServer() && Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return false;
        }

        if (hasBypassPermission(targetObj, isOperator)) {
            return true;
        }

        if (Storage.getPermissionPlugin() == PermissionPlugin.NONE) {
            return false;
        }

        return hasPermission(targetObj, "bypass." + command, isOperator);
    }

    public static boolean hasPermissionWithResponse(
            final Object targetObj,
            final String command,
            final boolean isOperator
    ) {
        boolean permitted = hasPermission(targetObj, command, isOperator);

        if (!permitted && targetObj instanceof CommandSender) {
            String message = Storage.ConfigSections.Messages.NO_PERMISSION.MESSAGE;
            message = message.replace("%permission%", "proantitab." + command);

            ((CommandSender) targetObj).sendMessage(message);
        }

        
        return permitted;
    }
}
