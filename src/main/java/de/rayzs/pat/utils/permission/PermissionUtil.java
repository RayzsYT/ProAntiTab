package de.rayzs.pat.utils.permission;

import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.CommandSender;
import java.util.*;

public class PermissionUtil {

    private static final HashMap<UUID, PermissionMap> MAP = new HashMap<>();

    public static void resetPermissions() {
        MAP.forEach((key, value) -> value.clear());
    }

    public static void resetPermissions(UUID uuid) {
        if(!MAP.containsKey(uuid)) return;
        MAP.get(uuid).clear();
    }

    public static boolean hasPermission(Object targetObj, String permission) {
        CommandSender sender;

        if(targetObj instanceof CommandSender) sender = (CommandSender) targetObj;
        else sender = new CommandSender(targetObj);

        PermissionMap permissionMap = MAP.getOrDefault(sender.getUniqueId(), new PermissionMap(sender.getUniqueId()));
        MAP.putIfAbsent(sender.getUniqueId(), permissionMap);

        if(!permissionMap.hasPermissionState("proantitab.*"))
            permissionMap.setState(permission, sender.hasPermission("proantitab.*"));

        if(permissionMap.isPermitted("proantitab.*")) return true;

        if(!permissionMap.hasPermissionState("proantitab." + permission))
            permissionMap.setState(permission, sender.hasPermission("proantitab." + permission));

        return permissionMap.isPermitted("proantitab." + permission);
    }

    public static boolean hasBypassPermission(Object targetObj) {
        return hasPermission(targetObj, "bypass");
    }

    public static boolean hasBypassPermission(Object targetObj, String command) {
        if (command.contains(" ")) {
            String[] split = command.split(" ");
            if (split.length > 0)
                command = split[0];
            command = command.split(" ")[0];
        }

        return hasBypassPermission(targetObj)
                || hasPermission(targetObj, "bypass." + command.toLowerCase())
                || hasServerPermission(targetObj, "bypass." + command.toLowerCase())
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
                || hasServerPermission(targetObj, "bypass." + command.toLowerCase())
                || GroupManager.canAccessCommand(targetObj, command, server);
    }

    public static boolean hasServerPermission(Object targetObj, String permission) {
        StringBuilder builder = new StringBuilder();
        CommandSender sender = new CommandSender(targetObj);
        String targetServerName = sender.getServerName();
        boolean allowed = false, numeric = false;
        if(targetServerName == null) return false;

        if(!Character.isDigit(targetServerName.charAt(0)))
            for (char c : targetServerName.toCharArray()) {
                if (Character.isDigit(c)) {
                    numeric = true;
                    break;
                }

                builder.append(c);
            }

        if(numeric) allowed = hasPermission(targetObj, permission + "." + builder + ".*");
        if(!allowed) allowed = hasPermission(targetObj, permission + "." + targetServerName);

        return allowed;
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
