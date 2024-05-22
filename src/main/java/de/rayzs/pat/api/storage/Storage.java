package de.rayzs.pat.api.storage;

import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.api.storage.config.messages.*;
import de.rayzs.pat.api.storage.config.settings.*;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.configuration.*;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.*;

public class Storage {

    public static final List<UUID> NOTIFY_PLAYERS = new ArrayList<>();

    public static String TOKEN = "", SERVER_NAME = null, CURRENT_VERSION = "", NEWER_VERSION = "";
    public static boolean OUTDATED = false, SEND_CONSOLE_NOTIFICATION = true;

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
                TOKEN = Configurator.get("token");

        public static void initialize() {}
    }

    public static class ConfigSections {

        public static List<ConfigStorage> SECTIONS = new ArrayList<>();

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

            public static BlacklistSection BLACKLIST = new BlacklistSection();
            public static CommandFailedSection COMMAND_FAILED = new CommandFailedSection();
            public static GroupSection GROUP = new GroupSection();
            public static HelpSection HELP = new HelpSection();
            public static NoPermissionSection NO_PERMISSION = new NoPermissionSection();
            public static NotificationSection NOTIFICATION = new NotificationSection();
            public static OnlyForProxySection NO_PROXY = new OnlyForProxySection();
            public static ReloadSection RELOAD = new ReloadSection();
            public static StatsSection STATS = new StatsSection();

            public static void initialize() {}
        }
    }

    public static class Blacklist {

        private static HashMap<String, GeneralBlacklist> SERVER_BLACKLISTS = new HashMap<>();
        private static final GeneralBlacklist BLACKLIST = BlacklistCreator.createGeneralBlacklist();

        public static void loadAll() {
            SERVER_BLACKLISTS.clear();
            BLACKLIST.load();

            if(!Reflection.isProxyServer()) return;

            BLACKLIST.getConfig().getKeys("general.servers", true).forEach(key -> {
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

        public static List<GeneralBlacklist> getBlacklists(String server) {
            List<GeneralBlacklist> blacklists = new ArrayList<>();
            SERVER_BLACKLISTS.entrySet().stream().filter(entry -> entry.getKey().endsWith("*") ? isServer(entry.getKey(), server) : entry.getKey().equals(server)).forEach(entry -> blacklists.add(entry.getValue()));
            return blacklists;
        }

        public static boolean isListed(String command, String server) {
            boolean blocked = isBlocked(command, server);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isBlocked(command, server);
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
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getCommands(server), intensive) && group.hasPermission(playerObj))) return true;
            return false;
        }

        public static boolean isListed(Object playerObj, String command, boolean intensive, String server) {
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getCommands(server), intensive) && group.hasPermission(playerObj))) return false;

            boolean blocked = isListed(command, intensive);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isListed(command, intensive);
                if(blocked) break;
            }
            return blocked;
        }

        public static boolean isBlocked(Object playerObj, String command, String server) {
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getCommands(server), false) && group.hasPermission(playerObj))) return false;

            boolean blocked = isBlocked(playerObj, command);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isBlocked(playerObj, command);
                if(blocked) break;
            }
            return blocked;
        }

        public static boolean isBlocked(Object targetObj, String command, boolean intensive, String server) {
            if(GroupManager.getGroupsByServer(server).stream().anyMatch(group -> isInListed(command, group.getCommands(server), intensive) && group.hasPermission(targetObj))) return false;

            boolean blocked = isBlocked(targetObj, command, intensive);
            if(!blocked) for (GeneralBlacklist blacklist : getBlacklists(server)) {
                blocked = blacklist.isBlocked(targetObj, command, intensive);
                if(blocked) break;
            }
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

        public static boolean isNotTabable(Object targetObj, String command) {
            return BLACKLIST.isBlocked(targetObj, command, !ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED);
        }

        public static boolean isInListed(String command, List<String> commandList, boolean intensive) {
            command = command.toLowerCase();
            if(command.startsWith("/")) command = command.replaceFirst("/", "");

            String[] split;
            if(command.contains(" ")) {
                split = command.split(" ");
                if(split.length > 0) command = split[0];
                command = command.split(" ")[0];
            }

            if(intensive && command.contains(":")) {
                split = command.split(":");
                if(split.length > 0)
                    command = command.replaceFirst(split[0] + ":", "");
            }

            for (String commands : commandList)
                if(commands.equals(command)) return true;

            return false;
        }
    }
}
