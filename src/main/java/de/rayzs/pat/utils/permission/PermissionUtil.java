package de.rayzs.pat.utils.permission;

import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import java.util.*;

public class PermissionUtil {

    private static final HashMap<UUID, PermissionMap> MAP = new HashMap<>();

    public static void resetPermissions() {
        MAP.forEach((key, value) -> value.clear());
    }

    public static void reloadPermissions() {
        MAP.keySet().forEach(PermissionUtil::reloadPermissions);
    }

    public static void reloadPermissions(UUID uuid) {
        if(!MAP.containsKey(uuid)) return;

        MAP.get(uuid).clear();
        setPlayerPermissions(uuid);
    }

    public static String getPermissionsAsString(UUID uuid) {
        PermissionMap permissionMap = MAP.get(uuid);
        if(permissionMap == null) return "";
        return Arrays.toString(permissionMap.getHashedPermissions().toArray()).replace("[", "").replace("]", "");
    }

    public static Set<String> getPermissions(UUID uuid) {
        PermissionMap permissionMap = MAP.get(uuid);
        if(permissionMap == null) return null;
        return permissionMap.getHashedPermissions();
    }

    public static void setPlayerPermissions(UUID uuid) {
        if(Storage.USE_LUCKPERMS) LuckPermsAdapter.setPermissions(uuid);
    }

    public static void resetPermissions(UUID uuid) {
        if(!MAP.containsKey(uuid)) return;
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

    public static boolean hasPermission(Object targetObj, String permission) {
        CommandSender sender;
        PermissionMap permissionMap;
        UUID uuid;

        if(targetObj instanceof CommandSender) sender = (CommandSender) targetObj;
        else sender = new CommandSender(targetObj);

        if(sender.isConsole()) return true;

        uuid = sender.getUniqueId();

        if(!MAP.containsKey(uuid)) {
            MAP.put(uuid, new PermissionMap(sender.getUniqueId()));
            return false;
        }

        permissionMap = MAP.get(uuid);

        if(!Storage.USE_LUCKPERMS) {
            if (permissionMap.hasPermissionState("*"))
                permissionMap.setState("*", sender.hasPermission("*"));

            if (permissionMap.hasPermissionState("proantitab.*"))
                permissionMap.setState("proantitab.*", sender.hasPermission("proantitab.*"));

            if (permissionMap.hasPermissionState("proantitab." + permission))
                permissionMap.setState("proantitab." + permission, sender.hasPermission("proantitab." + permission));
        }

        if(sender.isOperator())
            if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
                return true;

        return permissionMap.isPermitted("*") || permissionMap.isPermitted("proantitab.*") || permissionMap.isPermitted("proantitab." + permission);
    }

    public static boolean hasBypassPermission(Object targetObj) {
        return hasPermission(targetObj, "bypass");
    }

    public static boolean hasBypassPermission(Object targetObj, String command, boolean slash) {
        command = Storage.Blacklist.convertCommand(command, false, true, slash);

        return hasBypassPermission(targetObj)
                || hasPermission(targetObj, "bypass." + command.toLowerCase())
                || GroupManager.canAccessCommand(targetObj, command, slash);
    }

    public static boolean hasBypassPermission(Object targetObj, String command, boolean slash, String server) {
        command = Storage.Blacklist.convertCommand(command, false, true, slash);

        return hasBypassPermission(targetObj)
                || hasPermission(targetObj, "bypass." + command.toLowerCase())
                || GroupManager.canAccessCommand(targetObj, command, slash, server);
    }

    public static boolean hasBypassPermission(Object targetObj, String command) {
        command = Storage.Blacklist.convertCommand(command, false, true);

        return hasBypassPermission(targetObj)
                || hasPermission(targetObj, "bypass." + command.toLowerCase())
                || GroupManager.canAccessCommand(targetObj, command);
    }

    public static boolean hasBypassPermission(Object targetObj, String command, String server) {
        server = server.toLowerCase();
        if (command.contains(" ")) {
            String[] split = command.split(" ");
            if (split.length > 0)
                command = split[0];
            command = command.split(" ")[0];
        }

        return hasBypassPermission(targetObj)
                || hasPermission(targetObj, "bypass." + command.toLowerCase())
                || GroupManager.canAccessCommand(targetObj, command, server);
    }

    public static boolean hasPermissionWithResponse(Object targetObj, String command) {
        boolean permitted = hasPermission(targetObj, command);
        if(!permitted) {
            if(targetObj instanceof CommandSender)
                ((CommandSender) targetObj).sendMessage(Storage.ConfigSections.Messages.NO_PERMISSION.MESSAGE.replace("%permission%", "proantitab." + command));
        }
        return permitted;
    }
}
