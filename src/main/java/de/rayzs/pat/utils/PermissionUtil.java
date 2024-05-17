package de.rayzs.pat.utils;

import de.rayzs.pat.plugin.BukkitLoader;
import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.List;

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
                || hasServerPermission(targetObj, "bypass." + command.toLowerCase())
                || GroupManager.canAccessCommand(targetObj, command);
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
                ((CommandSender) targetObj).sendMessage(Storage.NO_PERMISSIONS.replace("%permission%", "proantitab." + command));
        }
        return permitted;
    }
}
