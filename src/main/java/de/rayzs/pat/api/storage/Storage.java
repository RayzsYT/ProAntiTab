package de.rayzs.pat.api.storage;

import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.*;
import de.rayzs.pat.api.storage.placeholders.commands.general.*;
import de.rayzs.pat.api.storage.placeholders.commands.group.*;
import de.rayzs.pat.api.storage.placeholders.general.GeneralCurrentVersionPlaceholder;
import de.rayzs.pat.api.storage.placeholders.general.GeneralNewestVersionPlaceholder;
import de.rayzs.pat.api.storage.placeholders.general.GeneralUserPlaceholder;
import de.rayzs.pat.api.storage.placeholders.groups.*;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.api.storage.config.messages.*;
import de.rayzs.pat.api.storage.config.settings.*;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.plugin.*;
import de.rayzs.pat.utils.StringUtils;
import de.rayzs.pat.utils.configuration.*;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.*;
import org.bukkit.entity.Player;

import java.util.*;

public class Storage {

    public static final List<UUID> NOTIFY_PLAYERS = new ArrayList<>();

    public static String TOKEN = "", SERVER_NAME = null, CURRENT_VERSION = "", NEWER_VERSION = "";
    public static boolean OUTDATED = false, SEND_CONSOLE_NOTIFICATION = true;
    public static Object PLUGIN_OBJECT;
    public static boolean USE_LUCKPERMS = false, USE_PLACEHOLDERAPI = false, USE_PAPIPROXYBRIDGE = false, USE_VIAVERSION = false, USE_VELOCITY = false;

    public static void loadAll(boolean loadBlacklist) {
        loadConfig();
        loadToken();
        if(loadBlacklist) Blacklist.loadAll();
    }

    public static void loadToken() {
        TOKEN = Reflection.isProxyServer() ? (String) Files.TOKEN.getOrSet("token", UUID.randomUUID().toString()) : ConfigSections.Settings.HANDLE_THROUGH_PROXY.TOKEN;
    }

    public static void loadConfig() {
        ConfigSections.Settings.initialize();
        ConfigSections.Messages.initialize();
        ConfigSections.SECTIONS.forEach(ConfigStorage::load);

        if(USE_PLACEHOLDERAPI)
            ConfigSections.PLACEHOLDERS.forEach(PlaceholderStorage::load);
    }

    public static List<String> getServers() {
        return getServers(false);
    }

    public static List<String> getServers(boolean withBlacklist) {
        List<String> servers = new ArrayList<>();
        if(Reflection.isVelocityServer())
            servers.addAll(VelocityLoader.getServerNames());
        else if(Reflection.isProxyServer())
            servers.addAll(BungeeLoader.getServerNames());

        if(withBlacklist) Blacklist.getBlacklistServers().stream().filter(server -> !servers.contains(server)).forEach(servers::add);
        return servers;
    }

    public static boolean isServer(String originServer, Set<String> targetServers) {
        List<String> list = new ArrayList<>(targetServers);
        return isServer(originServer, list);
    }

    public static boolean isServer(String originServer, List<String> targetServers) {
        for (String targetServer : targetServers)
            if(isServer(originServer, targetServer))
                return true;
        return false;
    }

    public static boolean isServer(String originServer, String targetServer) {
        originServer = originServer.toLowerCase();
        targetServer = targetServer.toLowerCase();

        if(originServer.endsWith("*")) {
            originServer = originServer.replace("*", "");
            return targetServer.startsWith(originServer);
        }
        return originServer.equals(targetServer);
    }

    public static class Files {
        public static final ConfigurationBuilder
                CONFIGURATION = Configurator.get("config"),
                STORAGE = Configurator.get("storage"),
                PLACEHOLDERS = Configurator.get("placeholders"),
                TOKEN = Configurator.get("token");

        public static void initialize() {}
    }

    public static class ConfigSections {

        public static List<ConfigStorage> SECTIONS = new ArrayList<>();
        public static List<PlaceholderStorage> PLACEHOLDERS = new ArrayList<>();

        public static class Settings {

            public static HandleThroughProxySection HANDLE_THROUGH_PROXY = new HandleThroughProxySection();
            public static CustomBrandSection CUSTOM_BRAND = new CustomBrandSection();
            public static CancelCommandSection CANCEL_COMMAND = new CancelCommandSection();
            public static CustomPluginsSection CUSTOM_PLUGIN = new CustomPluginsSection();
            public static CustomProtocolPingSection CUSTOM_PROTOCOL_PING = new CustomProtocolPingSection();
            public static CustomUnknownCommandSection CUSTOM_UNKNOWN_COMMAND = new CustomUnknownCommandSection();
            public static TurnBlacklistToWhitelistSection TURN_BLACKLIST_TO_WHITELIST = new TurnBlacklistToWhitelistSection();
            public static UpdateSection UPDATE = new UpdateSection();

            public static void initialize() {}
        }

        public static class Messages {

            public static PrefixSection PREFIX = new PrefixSection();
            public static BlacklistSection BLACKLIST = new BlacklistSection();
            public static CommandFailedSection COMMAND_FAILED = new CommandFailedSection();
            public static GroupSection GROUP = new GroupSection();
            public static HelpSection HELP = new HelpSection();
            public static NoPermissionSection NO_PERMISSION = new NoPermissionSection();
            public static NotificationSection NOTIFICATION = new NotificationSection();
            public static OnlyForProxySection NO_PROXY = new OnlyForProxySection();
            public static ReloadSection RELOAD = new ReloadSection();
            public static ServerListSection SERV_LIST = new ServerListSection();
            public static StatsSection STATS = new StatsSection();

            public static void initialize() {}
        }

        public static class Placeholders {

            public static GeneralUserPlaceholder USER = new GeneralUserPlaceholder();
            public static GeneralCurrentVersionPlaceholder CURRENT_VERSION = new GeneralCurrentVersionPlaceholder();
            public static GeneralNewestVersionPlaceholder NEWEST_VERSION = new GeneralNewestVersionPlaceholder();

            public static ListGroupsPlaceholder LIST_GROUP = new ListGroupsPlaceholder();
            public static ListGroupsReversedPlaceholder LIST_GROUP_REVERSED = new ListGroupsReversedPlaceholder();
            public static ListGroupsSortedPlaceholder LIST_GROUP_SORTED = new ListGroupsSortedPlaceholder();
            public static ListSizeGroupsPlaceholder LIST_SIZE_GROUPS = new ListSizeGroupsPlaceholder();

            public static ListGroupCommandsPlaceholder LIST_GROUP_COMMANDS = new ListGroupCommandsPlaceholder();
            public static ListGroupReversedCommandsPlaceholder LIST_GROUP_REVERSED_COMMANDS = new ListGroupReversedCommandsPlaceholder();
            public static ListGroupSortedCommandsPlaceholder LIST_GROUP_SORTED_COMMANDS = new ListGroupSortedCommandsPlaceholder();
            public static ListGroupSizeCommandsPlaceholder LIST_GROUP_SIZE_GROUP = new ListGroupSizeCommandsPlaceholder();

            public static ListCommandsPlaceholder LIST_COMMANDS = new ListCommandsPlaceholder();
            public static ListReversedCommandsPlaceholder LIST_REVERSED_COMMANDS = new ListReversedCommandsPlaceholder();
            public static ListSortedCommandsPlaceholder LIST_SORTED_COMMANDS = new ListSortedCommandsPlaceholder();
            public static ListSizeCommandsPlaceholder LIST_SIZE_COMMANDS = new ListSizeCommandsPlaceholder();

            public static void initialize() {}

            public static String findAndReplace(Player player, String request) {
                String result = null, param = "";

                if(USE_PLACEHOLDERAPI)
                    for (PlaceholderStorage storage : PLACEHOLDERS) {
                        if(!storage.getRequest().startsWith(request)) continue;

                        if(storage.getRequest().endsWith("group_") && request.contains("group_"))
                            param = request.split("group_")[1];

                        result = storage.onRequest(player, param);
                        if(result == null) continue;
                        else result = result.replace("\\n", "\n");

                        break;
                    }

                return result;
            }
        }
    }

    public static class Blacklist {

        private static HashMap<String, GeneralBlacklist> SERVER_BLACKLISTS = new HashMap<>();
        private static final GeneralBlacklist BLACKLIST = BlacklistCreator.createGeneralBlacklist();

        public static void loadAll() {
            SERVER_BLACKLISTS.clear();
            BLACKLIST.load();

            if(!Reflection.isProxyServer()) return;

            BLACKLIST.getConfig().getKeys("global.servers", true).forEach(key -> {
                GeneralBlacklist blacklist = BlacklistCreator.createGeneralBlacklist(key);
                blacklist.load();
                SERVER_BLACKLISTS.put(key, blacklist);
            });
        }

        public static List<String> getBlacklistServers() {
            return new ArrayList<>(SERVER_BLACKLISTS.keySet());
        }

        public static GeneralBlacklist getBlacklist() {
            return BLACKLIST;
        }

        public static GeneralBlacklist getBlacklist(String server) {
            GeneralBlacklist blacklist;

            if(!SERVER_BLACKLISTS.containsKey(server)) {
                blacklist = BlacklistCreator.createGeneralBlacklist(server);
                blacklist.load();
                SERVER_BLACKLISTS.put(server, blacklist);
            } else blacklist = SERVER_BLACKLISTS.get(server);

            return SERVER_BLACKLISTS.getOrDefault(server, blacklist);
        }

        public static List<GeneralBlacklist> getAllBlacklists(String server) {
            List<GeneralBlacklist> blacklists = getBlacklists(server);
            blacklists.add(Storage.Blacklist.getBlacklist());
            return blacklists;
        }

        public static List<GeneralBlacklist> getBlacklists(String server) {
            List<GeneralBlacklist> blacklists = new ArrayList<>();
            SERVER_BLACKLISTS.entrySet().stream().filter(entry -> entry.getKey().endsWith("*") ? isServer(entry.getKey(), server) : entry.getKey().equals(server)).forEach(entry -> blacklists.add(entry.getValue()));
            return blacklists;
        }

        public static boolean isListed(String command, String server) {
            boolean blocked = isBlocked(command, server);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isBlocked(command, server, !ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
                if(blocked) break;
            }
            return blocked;
        }

        public static boolean isListed(String command, boolean intensive, String server) {
            boolean blocked = isListed(command, intensive);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isListed(command, intensive);
                if(blocked) break;
            }
            return blocked;
        }

        public static boolean doesGroupBypass(Object playerObj, String command, boolean intensive, String server) {
            for (Group group : GroupManager.getGroups()) {
                for (GroupBlacklist groupBlacklist : group.getAllServerGroupBlacklist(server)) {
                    if(groupBlacklist.isListed(command, intensive) && group.hasPermission(playerObj)) return true;
                }
            }

            return false;
        }

        public static boolean isListed(Object playerObj, String command, boolean intensive, String server) {
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getAllCommands(server), intensive) && group.hasPermission(playerObj))) return false;

            boolean blocked = isListed(command, intensive);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isListed(command, intensive);
                if(blocked) break;
            }
            return blocked;
        }

        public static boolean isBlocked(Object targetObj, String command, String server) {
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getAllCommands(server), !ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED) && group.hasPermission(targetObj)))
                return false;

            boolean blocked = isBlocked(targetObj, command);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isBlocked(targetObj, command, !ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
                if(blocked) break;
            }
            return blocked;
        }

        public static boolean isBlocked(Object targetObj, String command, boolean intensive, String server) {
            return isBlocked(targetObj, command, intensive, server, false);
        }

        public static boolean isBlocked(Object targetObj, String command, boolean intensive, String server, boolean focusOnBlock) {
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getAllCommands(server), intensive) && group.hasPermission(targetObj)))
                return false;

            boolean blocked = isBlocked(targetObj, command, intensive),
                    allow = false;

            if(focusOnBlock && blocked) return true;
            for (GeneralBlacklist blacklist : getBlacklists(server)) {
                if(!focusOnBlock) {
                    allow = !blacklist.isBlocked(targetObj, command, intensive);
                    if (allow) break;
                } else {
                    if(blacklist.isBlocked(targetObj, command, intensive)) return true;
                }
            }

            blocked = !allow && blocked;
            return blocked;
        }

        public static boolean isListed(String command) {
            return BLACKLIST.isListed(command);
        }

        public static boolean isListed(String command, boolean intensive) {
            return BLACKLIST.isListed(command, intensive);
        }

        public static boolean isBlocked(Object targetObj, String command) {
            return BLACKLIST.isBlocked(targetObj, command);
        }

        public static boolean isBlocked(Object targetObj, String command, boolean intensive) {
            return BLACKLIST.isBlocked(targetObj, command, intensive);
        }

        public static boolean isBlocked(String command, boolean intensive) {
            return BLACKLIST.isBlocked(command, intensive);
        }

        public static boolean isConverted(String command, boolean intensive) {
            return BLACKLIST.isConverted(command, intensive);
        }

        public static String convertCommand(String command, boolean intensive, boolean lowercase) {
            return BLACKLIST.convertCommand(command, intensive, lowercase);
        }

        public static boolean isInListed(String command, List<String> commandList, boolean intensive) {
            command = StringUtils.replaceFirst(command, "/", "");
            command = convertCommand(command, intensive, false);

            for (String commands : commandList)
                if(commands.equals(command)) return true;

            return false;
        }
    }
}
