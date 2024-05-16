package de.rayzs.pat.utils;

import de.rayzs.pat.utils.configuration.*;
import java.util.*;

public class Storage {

    public static String CURRENT_VERSION_NAME = "", NEWEST_VERSION_NAME = "";
    public static ConfigurationBuilder CONFIGURATION = Configurator.get("config"), STORAGE = Configurator.get("storage"), TOKEN = Configurator.get("token");
    public static List<String> UNKNOWN_COMMAND, CANCEL_COMMANDS_MESSAGE, STATS, BLOCKED_COMMANDS_LIST, COMMAND_HELP = new ArrayList<>(), CUSTOM_SERVER_BRANDS = new ArrayList<>(), UPDATE_NOTIFICATION = new ArrayList<>();
    public static List<UUID> NOTIFY_PLAYERS = new ArrayList<>();
    public static String TOKEN_KEY = UUID.randomUUID().toString(), BUNGEECORD_MESSAGE, NOTIFY_ALERT, NOTIFY_ENABLED, NOTIFY_DISABLED, COMMAND_UNKNOWN, NO_PERMISSIONS, RELOAD_LOADING, RELOAD_DONE,
            BLACKLIST_CLEAR_CONFIRM_MESSAGE, BLACKLIST_CLEAR_MESSAGE, BLACKLIST_LIST_COMMAND_MESSAGE, BLACKLIST_ADD_MESSAGE, BLACKLIST_ADD_FAIL_MESSAGE, BLACKLIST_REMOVE_MESSAGE, BLACKLIST_REMOVE_FAIL_MESSAGE, BLACKLIST_LIST_MESSAGE, BLACKLIST_LIST_SPLITTER_MESSAGE,
            GROUP_DELETE_CONFIRM_MESSAGE, GROUP_CREATE_MESSAGE, GROUP_DELETE_MESSAGE, GROUP_ALREADY_CREATED_MESSAGE, GROUP_NOT_EXIST_MESSAGE, GROUP_CLEAR_MESSAGE, GROUP_LIST_COMMAND_MESSAGE, GROUP_ADD_MESSAGE, GROUP_ADD_FAIL_MESSAGE, GROUP_REMOVE_MESSAGE, GROUP_REMOVE_FAIL_MESSAGE, GROUP_LIST_MESSAGE, GROUP_LIST_SPLITTER_MESSAGE, GROUPS_LIST_MESSAGE, GROUPS_LIST_SPLITTER_MESSAGE, GROUPS_LIST_GROUPS_MESSAGE,
            GROUP_CLEAR_CONFIRM_MESSAGE,
            STATS_FAIL_MESSAGE, STATS_SERVERS_SPLITTER_MESSAGE, STATS_SERVERS_MESSAGE, STATS_SERVERS_NO_SERVER_MESSAGE;
    public static boolean BUNGEECORD = false, USE_CUSTOM_BRAND, USE_UNKNOWN_COMMAND, TURN_BLACKLIST_TO_WHITELIST, CANCEL_COMMANDS, UPDATE_ENABLED, OUTDATED_VERSION = false, CONSOLE_NOTIFICATION_ENABLED = true;
    public static int UPDATE_PERIOD, SERVER_DATA_SYNC_COUNT = 0, CUSTOM_SERVER_BRAND_REPEAT_DELAY;
    public static long LAST_DATA_UPDATE = System.currentTimeMillis();

    public static void
    load() {
        STORAGE.reload();
        CONFIGURATION.reload();

        ArrayList<String> defaultBlockedCommandArray = new ArrayList<>(),
                defaultHelpArray = new ArrayList<>(),
                defaultUnknownCommandArray = new ArrayList<>(),
                defaultNotificationArray = new ArrayList<>(),
                statsMessageArray = new ArrayList<>(),
                customServerBrandsArray = new ArrayList<>(),
                commandBlockedArray = new ArrayList<>();

        defaultBlockedCommandArray.addAll(Arrays.asList("help", "?", "about", "ver", "version", "icanhasbukkit", "pl", "plugins"));
        defaultHelpArray.add("&7Available commands are: &f/%label%&7...");
        defaultHelpArray.add("&f  reload &7to reload the plugin");
        defaultHelpArray.add("&f  notify &7to get alerted");
        defaultHelpArray.add("&f  listgroups &7List all groups");
        defaultHelpArray.add("&f  creategroup (group) &7Create a group");
        defaultHelpArray.add("&f  deletegroup (group) &7Delete a group");
        defaultHelpArray.add("&f  list &8(optional: group) &7to see all listed commands");
        defaultHelpArray.add("&f  clear &8(optional: group) &7to clear the list");
        defaultHelpArray.add("&f  add/remove (command) &8(optional: group) &7to manage the list");

        defaultUnknownCommandArray.add("&cThis command does not exist!");

        defaultNotificationArray.add("&8[&4ProAntiTab&8] &cThere is a new version available! (%newest_version%)");
        defaultNotificationArray.add("&8[&4ProAntiTab&8] &cYou are still using the %current_version%.");
        defaultNotificationArray.add("&8[&4ProAntiTab&8] &cGet the newest version here:");
        defaultNotificationArray.add("&8[&4ProAntiTab&8] &ehttps://www.rayzs.de/products/proantitab/page");

        statsMessageArray.add("&7Last sync sent to &f%server_count% &7servers. &8&o(%last_sync_time% ago)");
        statsMessageArray.add("&7Sent to servers: &f%servers%");

        customServerBrandsArray.add("&f&lP&froAntiTab |");
        customServerBrandsArray.add("&fP&lr&foAntiTab /");
        customServerBrandsArray.add("&fPr&lo&fAntiTab -");
        customServerBrandsArray.add("&fPro&lA&fntiTab |");
        customServerBrandsArray.add("&fProA&ln&ftiTab \\");
        customServerBrandsArray.add("&fProAn&lt&fiTab |");
        customServerBrandsArray.add("&fProAnt&li&fTab /");
        customServerBrandsArray.add("&fProAnti&lT&fab -");
        customServerBrandsArray.add("&fProAnti&lT&fab \\");
        customServerBrandsArray.add("&fProAntiT&la&fb |");
        customServerBrandsArray.add("&fProAntiTa&lb&f /");
        customServerBrandsArray.add("&fProAntiTab -");
        customServerBrandsArray.add("&fProAntiTab \\");

        commandBlockedArray.add("The command %command% is blocked!");

        UPDATE_ENABLED = (boolean) CONFIGURATION.getOrSet("updater.enabled", true);
        UPDATE_PERIOD = (int) CONFIGURATION.getOrSet("updater.period", 18000);

        UPDATE_NOTIFICATION = (ArrayList<String>) CONFIGURATION.getOrSet("updater.notification", defaultNotificationArray);
        STATS =  (ArrayList<String>) CONFIGURATION.getOrSet("stats.message.statistic", statsMessageArray);

        CUSTOM_SERVER_BRANDS = (ArrayList<String>) CONFIGURATION.getOrSet("custom-server-brand.brands", customServerBrandsArray);

        COMMAND_HELP = (ArrayList<String>) CONFIGURATION.getOrSet("help", defaultHelpArray);
        BLOCKED_COMMANDS_LIST = (ArrayList<String>) STORAGE.getOrSet("commands", defaultBlockedCommandArray);

        CANCEL_COMMANDS_MESSAGE = (ArrayList) CONFIGURATION.getOrSet("cancel-blocked-commands.message", commandBlockedArray);

        NO_PERMISSIONS = (String) CONFIGURATION.getOrSet("no-permissions", "&cYou are not allowed to execute this command! Missing permission: &4proantitab.%permission%");

        if(!Reflection.isProxyServer()) {
            BUNGEECORD = (boolean) CONFIGURATION.getOrSet("handle-through-proxy.enabled", BUNGEECORD);
            BUNGEECORD_MESSAGE = (String) CONFIGURATION.getOrSet("handle-through-proxy.message", BUNGEECORD_MESSAGE);
            USE_UNKNOWN_COMMAND = (boolean) CONFIGURATION.getOrSet("custom-unknown-command.enabled", USE_UNKNOWN_COMMAND);
            UNKNOWN_COMMAND = (ArrayList<String>) CONFIGURATION.getOrSet("custom-unknown-command.message", defaultUnknownCommandArray);
        }

        if(Reflection.isProxyServer()) {
            if(CONFIGURATION.get("token") != null) {
                TOKEN_KEY = (String) CONFIGURATION.get("token");
                TOKEN.setAndSave("token", TOKEN_KEY);
            } else TOKEN_KEY = (String) TOKEN.getOrSet("token", TOKEN_KEY);
        } else TOKEN_KEY = (String) CONFIGURATION.getOrSet("handle-through-proxy.token", "insert-token-of-proxy-here");

        NOTIFY_ENABLED = (String) CONFIGURATION.getOrSet("notification.enabled", "&aEnabled notifications!");
        NOTIFY_DISABLED = (String) CONFIGURATION.getOrSet("notification.disabled", "&cDisabled notifications!");
        NOTIFY_ALERT = (String) CONFIGURATION.getOrSet("notification.alert", "&8[&4ALERT&8] &c%player% tried to execute the following blocked command: &4%command%");

        STATS_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("stats.fail", "&cThis command works on Bungeecord/Velocity servers only!");
        STATS_SERVERS_NO_SERVER_MESSAGE = (String) CONFIGURATION.getOrSet("stats.no-server", "&cNone!");
        STATS_SERVERS_SPLITTER_MESSAGE = (String) CONFIGURATION.getOrSet("stats.message.splitter", "&7, ");
        STATS_SERVERS_MESSAGE = (String) CONFIGURATION.getOrSet("stats.message.server", "&f%servername% &8(%updated%)");

        USE_CUSTOM_BRAND = (boolean) CONFIGURATION.getOrSet("custom-server-brand.enabled", false);
        CUSTOM_SERVER_BRAND_REPEAT_DELAY = (int) CONFIGURATION.getOrSet("custom-server-brand.repeat-delay", Reflection.isProxyServer() ? 150 : 3);

        CANCEL_COMMANDS = (boolean) CONFIGURATION.getOrSet("cancel-blocked-commands.enabled", true);

        TURN_BLACKLIST_TO_WHITELIST = (boolean) CONFIGURATION.getOrSet("turn-blacklist-to-whitelist", false);
        BLACKLIST_CLEAR_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.clear", "&aList has been cleared!");
        BLACKLIST_CLEAR_CONFIRM_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.clear-confirmation", "&4Warning! &7This command will &cclear the entire list&7! &7Repeat the &esame command &7to confirm this action.");
        BLACKLIST_LIST_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.list.message", "&7Listed commands (&f%size%&7)&8: &f%commands%");
        BLACKLIST_LIST_SPLITTER_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.list.splitter", "&7, ");
        BLACKLIST_LIST_COMMAND_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.list.command", "&f");
        BLACKLIST_ADD_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.add.success", "&aSuccessfully added %command% into list!");
        BLACKLIST_ADD_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.add.failed", "&c%command% is already in the list!");
        BLACKLIST_REMOVE_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.remove.success", "&aSuccessfully removed %command% from the list!");
        BLACKLIST_REMOVE_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("blacklist.remove.failed", "&c%command% is not listed!");

        GROUPS_LIST_MESSAGE = (String) CONFIGURATION.getOrSet("group.list-groups.message", "&7All groups (&f%size%&7)&8: &f%groups%");
        GROUPS_LIST_SPLITTER_MESSAGE = (String) CONFIGURATION.getOrSet("group.list-groups.splitter", "&7, ");
        GROUPS_LIST_GROUPS_MESSAGE = (String) CONFIGURATION.getOrSet("group.list-groups.group", "&f");


        GROUP_CREATE_MESSAGE = (String) CONFIGURATION.getOrSet("group.create", "&aGroup %group% has been created!");
        GROUP_ALREADY_CREATED_MESSAGE = (String) CONFIGURATION.getOrSet("group.already-exist", "&cGroup %group% already exist!");
        GROUP_NOT_EXIST_MESSAGE = (String) CONFIGURATION.getOrSet("group.does-not-exist", "&cGroup %group% does not exist!");
        GROUP_DELETE_MESSAGE = (String) CONFIGURATION.getOrSet("group.delete", "&cGroup %group% has been deleted!");
        GROUP_DELETE_CONFIRM_MESSAGE = (String) CONFIGURATION.getOrSet("group.delete-confirmation", "&4Warning! &7This command will &cdelete the group with the whole list&7 of this group! &7Repeat the &esame command &7to confirm this action.");
        GROUP_CLEAR_MESSAGE = (String) CONFIGURATION.getOrSet("group.clear", "&aList of group %group% has been cleared!");
        GROUP_CLEAR_CONFIRM_MESSAGE = (String) CONFIGURATION.getOrSet("group.clear-confirmation", "&4Warning! &7This command will &cclear the entire list&7 of this group! &7Repeat the &esame command &7to confirm this action.");
        GROUP_LIST_MESSAGE = (String) CONFIGURATION.getOrSet("group.list.message", "&7Listed commands of group %group% (&f%size%&7)&8: &f%commands%");
        GROUP_LIST_SPLITTER_MESSAGE = (String) CONFIGURATION.getOrSet("group.list.splitter", "&7, ");
        GROUP_LIST_COMMAND_MESSAGE = (String) CONFIGURATION.getOrSet("group.list.command", "&f");
        GROUP_ADD_MESSAGE = (String) CONFIGURATION.getOrSet("group.add.success", "&aSuccessfully added %command% into the list of group %group%!");
        GROUP_ADD_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("group.add.failed", "&c%command% is already in the list of group %group%!");
        GROUP_REMOVE_MESSAGE = (String) CONFIGURATION.getOrSet("group.remove.success", "&aSuccessfully removed %command% from the list of group %group%!");
        GROUP_REMOVE_FAIL_MESSAGE = (String) CONFIGURATION.getOrSet("group.remove.failed", "&c%command% is not listed in the group %group%!");

        COMMAND_UNKNOWN = (String) CONFIGURATION.getOrSet("command-failed", "&cFailed to execute this command! Use '/pat' to see all available commands.");
        RELOAD_LOADING = (String) CONFIGURATION.getOrSet("reload.loading", "&eReloading all configuration files...");
        RELOAD_DONE = (String) CONFIGURATION.getOrSet("reload.done", "&aSuccessfully reloaded all configuration files!");
    }

    public static void saveStorage() {
        STORAGE.setAndSave("commands", BLOCKED_COMMANDS_LIST);
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
