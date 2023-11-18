package de.rayzs.pat.utils;

import de.rayzs.pat.utils.configuration.*;
import java.util.*;

public class Storage {

    public static ConfigurationBuilder CONFIGURATION = Configurator.get("config");
    public static ArrayList<String> BLOCKED_COMMANDS_LIST, COMMAND_HELP = new ArrayList<>();
    public static List<UUID> NOTIFY_PLAYERS = new ArrayList<>();
    public static String NOTIFY_ALERT, NOTIFY_ENABLED, NOTIFY_DISABLED, CANCEL_COMMANDS_MESSAGE, COMMAND_UNKNOWN, NO_PERMISSIONS, RELOAD_LOADING, RELOAD_DONE, BLACKLIST_CLEAR_MESSAGE, BLACKLIST_LIST_COMMAND_MESSAGE, BLACKLIST_ADD_MESSAGE, BLACKLIST_ADD_FAIL_MESSAGE, BLACKLIST_REMOVE_MESSAGE, BLACKLIST_REMOVE_FAIL_MESSAGE, BLACKLIST_LIST_MESSAGE, BLACKLIST_LIST_SPLITTER_MESSAGE;
    public static boolean TURN_BLACKLIST_TO_WHITELIST, CANCEL_COMMANDS, UPDATE_ENABLED, OUTDATED_VERSION = false, CONSOLE_NOTIFICATION_ENABLED = true;
    public static int UPDATE_PERIOD;

    public static void load() {
        CONFIGURATION.reload();

        ArrayList<String> defaultBlockedCommandArray = new ArrayList<>(),
                defaultHelpArray = new ArrayList<>();

        defaultBlockedCommandArray.addAll(Arrays.asList("help", "?", "about", "ver", "version", "icanhasbukkit", "pl", "plugins"));
        defaultHelpArray.add("&7Available commands are: &f/%label%&7...");
        defaultHelpArray.add("&f  reload &7to reload the plugin");
        defaultHelpArray.add("&f  list &7to see all listed commands");
        defaultHelpArray.add("&f  add/remove (command) &7to manage the list");
        defaultHelpArray.add("&f  notify &7to get alerted");
        defaultHelpArray.add("&f  clear &7to clear the list");

        UPDATE_ENABLED = (boolean) CONFIGURATION.getOrSet("updater.enabled", true);
        UPDATE_PERIOD = (int) CONFIGURATION.getOrSet("updater.period", 18000);

        COMMAND_HELP = (ArrayList<String>) CONFIGURATION.getOrSet("settings.help", defaultHelpArray);
        BLOCKED_COMMANDS_LIST = (ArrayList<String>) CONFIGURATION.getOrSet("settings.blacklist.commands", defaultBlockedCommandArray);
        NO_PERMISSIONS = (String) CONFIGURATION.getOrSet("settings.no-permissions", "&cYou are not allowed to execute this command! Missing permission: &4proantitab.%permission%");

        NOTIFY_ENABLED = (String) CONFIGURATION.getOrSet("settings.notification.enabled", "&aEnabled notifications!");
        NOTIFY_DISABLED = (String) CONFIGURATION.getOrSet("settings.notification.disabled", "&cDisabled notifications!");
        NOTIFY_ALERT = (String) CONFIGURATION.getOrSet("settings.notification.alert", "&8[&4ALERT&8] &c%player% tried to execute the following blocked command: &4%command%");

        CANCEL_COMMANDS = (boolean) CONFIGURATION.getOrSet("settings.cancel-blocked-commands.enabled", true);
        CANCEL_COMMANDS_MESSAGE = (String) CONFIGURATION.getOrSet("settings.cancel-blocked-commands.message", "&cThe command '%command%' is blocked!");

        TURN_BLACKLIST_TO_WHITELIST = (boolean) CONFIGURATION.getOrSet("settings.blacklist.turn-blacklist-to-whitelist.enabled", false);
        BLACKLIST_CLEAR_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.clear.message", "&aList of blacklisted commands has been cleared!");
        BLACKLIST_LIST_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.list.message", "&7Blocked commands (&f%size%&7)&8: &f%commands%");
        BLACKLIST_LIST_SPLITTER_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.list.splitter", "&7, ");
        BLACKLIST_LIST_COMMAND_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.list.command", "&f");
        BLACKLIST_ADD_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.add.success", "&aSuccessfully added %command% into blocked-commands list!");
        BLACKLIST_ADD_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.add.fail", "&c%command% is already in the list!");
        BLACKLIST_REMOVE_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.remove.success", "&aSuccessfully removed %command% from blocked-commands list!");
        BLACKLIST_REMOVE_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("settings.blacklist.remove.fail", "&c%command% is not listed!");

        COMMAND_UNKNOWN = (String) CONFIGURATION.getOrSet("settings.unknown-command", "&cFailed to execute this command! Use '/pat' to see all available commands.");
        RELOAD_LOADING = (String) CONFIGURATION.getOrSet("settings.reload.loading", "&eReloading all configuration files...");
        RELOAD_DONE = (String) CONFIGURATION.getOrSet("settings.reload.done", "&aSuccessfully reloaded all configuration files!");
    }

    public static void save() {
        CONFIGURATION
                .set("updater.enabled", UPDATE_ENABLED)
                .set("updater.period", UPDATE_PERIOD)
                .set("settings.no-permissions", NO_PERMISSIONS)
                .set("settings.unknown-command", COMMAND_UNKNOWN)
                .set("settings.reload.loading", RELOAD_LOADING)
                .set("settings.reload.done", RELOAD_DONE)
                .set("settings.help", COMMAND_HELP)
                .set("settings.cancel-blocked-commands.enabled", CANCEL_COMMANDS)
                .set("settings.cancel-blocked-commands.message", CANCEL_COMMANDS_MESSAGE)
                .set("settings.blacklist.turn-blacklist-to-whitelist.enabled", TURN_BLACKLIST_TO_WHITELIST)
                .set("settings.blacklist.commands", BLOCKED_COMMANDS_LIST)
                .set("settings.blacklist.clear.message", BLACKLIST_CLEAR_MESSAGE)
                .set("settings.blacklist.add.success", BLACKLIST_ADD_MESSAGE)
                .set("settings.blacklist.remove.success", BLACKLIST_REMOVE_MESSAGE)
                .set("settings.blacklist.add.failed", BLACKLIST_ADD_FAIL_MESSAGE)
                .set("settings.blacklist.remove.failed", BLACKLIST_REMOVE_FAIL_MESSAGE)
                .set("settings.blacklist.list.message", BLACKLIST_LIST_MESSAGE)
                .set("settings.blacklist.list.command", BLACKLIST_LIST_COMMAND_MESSAGE)
                .set("settings.blacklist.list.splitter", BLACKLIST_LIST_SPLITTER_MESSAGE)
                .save();
    }

    public static boolean isCommandBlocked(String command) {
        String[] split;
        if(command.contains(" ")) {
            split = command.split(" ");
            if(split.length > 0) command = split[0];
            command = command.split(" ")[0];
        }
        if(command.contains(":")) {
            split = command.split(":");
            if(split.length > 0) command = split[1];
        }
        for (String blockedCommand : BLOCKED_COMMANDS_LIST) {
            if(blockedCommand.equals(command.toLowerCase())) return true;
        }
        return false;
    }

    public static boolean isCommandBlockedPrecise(String command) {
        String[] split;
        if(command.contains(" ")) {
            split = command.split(" ");
            if(split.length > 0) command = split[0];
            command = command.split(" ")[0];
        }
        for (String blockedCommand : BLOCKED_COMMANDS_LIST) {
            if(blockedCommand.equals(command.toLowerCase())) return true;
        }
        return false;
    }
}
