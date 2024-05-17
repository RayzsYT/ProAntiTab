package de.rayzs.pat.utils;

import de.rayzs.pat.utils.group.GroupManager;

public class PermissionUtil {

    public static boolean hasPermission(Object targetObj, String permission) {
        CommandSender sender;

        if(targetObj instanceof CommandSender) sender = (CommandSender) targetObj;
        else sender = new CommandSender(targetObj);

        return sender.hasPermission("proantitab.*") || sender.hasPermission("proantitab." + permission);
    }

    public static boolean hasBypassPermission(Object targetObj) {
        return hasPermission(targetObj, "bypass");
    }

    public static boolean hasBypassPermission(Object targetObj, String command) {
        return hasBypassPermission(targetObj)
                || hasPermission(targetObj, "bypass." + command.toLowerCase())
                || hasServerPermission(targetObj, "bypass." + command.toLowerCase(), Storage.SERVER_NAME)
                || GroupManager.canAccessCommand(targetObj, command);
    }

    public static boolean hasServerPermission(Object target, String permission, String server) {
        String targetServerName = Storage.SERVER_NAME;
        if(target instanceof CommandSender) targetServerName = ((CommandSender) target).getServerName();
        if(targetServerName == null) return false;

        String permissionString = permission + "." + Storage.SERVER_NAME;
        return hasPermission(target, permissionString) || hasPermission(target, permissionString + ".*") && server.startsWith(targetServerName);
    }

    public static boolean hasPermissionWithResponse(Object targetObj, String command) {
        boolean permitted = hasPermission(targetObj, command);
        if(!permitted) {
            if(targetObj instanceof CommandSender)
                ((CommandSender) targetObj).sendMessage(Storage.NO_PERMISSIONS.replace("%permission%", "proantitab." + command));
        }
        return permitted;
    }
}
