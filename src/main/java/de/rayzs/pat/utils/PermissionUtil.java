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
                || GroupManager.canAccessCommand(targetObj, command);
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
