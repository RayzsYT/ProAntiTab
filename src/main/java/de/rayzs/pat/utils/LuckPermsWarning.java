package de.rayzs.pat.utils;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.Set;

public class LuckPermsWarning {

    private static boolean ALREADY_ANNOUNCED = false;

    private static final Set<String> LUCKPERMS_PREFIXES = Set.of(
            "luckperms", "lp",
            "luckpermsbungee", "lpb",
            "luckpermsvelocity", "lpv"
    );

    private static final Set<String> LUCKPERMS_COMMANDS_SET = Set.of(
            "set", "unset",
            "settemp", "unsettemp"
    );

    private static final String LUCKPERMS_WARNING = "&6&l[ProAntiTab] " +
            "&e&lHey! &eYou just set a ProAntiTab permission using a world/server as context. " +
            "However, PAT won't be able to detect this as of now. To fix this, please go to your &6&oplugins/ProAntiTab/config.yml&e and set the option &6&o'%s'&e to &a&ltrue&e. " +
            "Afterwards, just execute '&6/%s reload'&e to apply the changes.";

    public static boolean shouldSendLuckPermsWarning(final CommandSender sender, final String command) {
        if (ALREADY_ANNOUNCED) {
            return false;
        }

        if (Storage.getPermissionPlugin() != PermissionPlugin.LUCKPERMS) {
            return false;
        }

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_WORLD.ENABLED || Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED) {
            return false;
        }

        if (!LUCKPERMS_PREFIXES.contains(StringUtils.getFirstArg(command))) {
            return false;
        }

        if (!sender.isConsole() && !PermissionUtil.hasPermission(sender, "*", false) && !sender.isOperator()) {
            return false;
        }


        final String[] commandArgs = command.split(" ");


        final String fth = getStrAtIndex(commandArgs, 5);
        if (!StringUtils.startsWithIgnoreCase("proantitab.", fth)) {
            return false;
        }

        final String thd = getStrAtIndex(commandArgs, 3);
        if (!thd.equalsIgnoreCase("permission")) {
            return false;
        }

        final String fst = getStrAtIndex(commandArgs, 1);
        if (!fst.equalsIgnoreCase("group") && !fst.equalsIgnoreCase("user")) {
            return false;
        }

        if (!LUCKPERMS_COMMANDS_SET.contains(getStrAtIndex(commandArgs, 4))) {
            return false;
        }

        for (int i = 6; i < commandArgs.length; i++) {
            final String context = getStrAtIndex(commandArgs, i);
            final boolean res = StringUtils.startsWithIgnoreCase("server=", context)
                                || StringUtils.startsWithIgnoreCase("world=", context);

            if (res) {
                return true;
            }
        }

        return false;
    }

    public static void sendAnnouncement(final CommandSender sender) {
        if (ALREADY_ANNOUNCED) return;

        ALREADY_ANNOUNCED = true;

        final String message = LUCKPERMS_WARNING.formatted(
                Reflection.isProxyServer() ? "update-groups-per-server" : "update-groups-per-world",
                Reflection.isProxyServer() ? "bpat" : "pat"
        );

        if (!sender.isConsole()) Logger.info(message);
        sender.sendMessage(message);
    }

    public static boolean alreadyAnnounced() {
        return ALREADY_ANNOUNCED;
    }

    private static String getStrAtIndex(final String[] args, final int index) {
        if (index < 0 || index >= args.length) return "";
        return args[index];
    }
}
