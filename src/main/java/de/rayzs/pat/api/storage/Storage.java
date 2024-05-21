package de.rayzs.pat.api.storage;

import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.api.storage.config.messages.*;
import de.rayzs.pat.api.storage.config.settings.*;
import de.rayzs.pat.utils.configuration.*;
import de.rayzs.pat.utils.Reflection;
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
            BLACKLIST.getConfig().getKeys(true).stream().filter(key -> key.startsWith("general.servers.")).forEach(key -> {
                String[] args = key.split("\\.");
                if(args.length >= 2) {
                    String serverName = args[2];
                    System.out.println("SERVERNAME: " + serverName);
                    getBlacklist(serverName);
                }
            });
        }

        public static GeneralBlacklist getBlacklist() {
            return BLACKLIST;
        }

        public static GeneralBlacklist getBlacklist(String server) {
            GeneralBlacklist blacklist = null;
            if(!SERVER_BLACKLISTS.containsKey(server)) {
                blacklist = BlacklistCreator.createGeneralBlacklist(server);
                blacklist.load();
                SERVER_BLACKLISTS.put(server, blacklist);
            }

            return SERVER_BLACKLISTS.getOrDefault(server, blacklist);
        }

        public static boolean isListed(String command, String server) {
            return BLACKLIST.isListed(command) || getBlacklist(server).isListed(command);
        }

        public static boolean isListed(String command, boolean intensive, String server) {
            return BLACKLIST.isListed(command, intensive) || getBlacklist(server).isListed(command, intensive);
        }

        public static boolean isBlocked(Object targetObj, String command, String server) {
            return isBlocked(targetObj, command) || getBlacklist(server).isBlocked(targetObj, command);
        }

        public static boolean isBlocked(Object targetObj, String command, boolean intensive, String server) {
            return isBlocked(targetObj, command, intensive) || getBlacklist(server).isBlocked(targetObj, command, intensive);
        }

        public static boolean isListed(String command) {
            return BLACKLIST.isListed(command);
        }

        public static boolean isListed(String command, boolean intensive) {
            return BLACKLIST.isListed(command, intensive);
        }

        public static boolean isBlocked(Object targetObj, String command) {
            return isBlocked(targetObj, command);
        }

        public static boolean isBlocked(Object targetObj, String command, boolean intensive) {
            return isBlocked(targetObj, command, intensive);
        }
    }
}
