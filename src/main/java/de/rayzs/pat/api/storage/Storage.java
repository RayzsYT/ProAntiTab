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

    public static final GeneralBlacklist BLACKLIST = BlacklistCreator.createGeneralBlacklist();
    public static final List<UUID> NOTIFY_PLAYERS = new ArrayList<>();

    public static String TOKEN, SERVER_NAME, CURRENT_VERSION, NEWER_VERSION;
    public static boolean OUTDATED = false, SEND_CONSOLE_NOTIFICATION = false;

    public static void loadAll(boolean loadBlacklist) {
        loadConfig();
        loadToken();
        if(loadBlacklist) BLACKLIST.load();
    }

    public static void loadToken() {
        TOKEN = Reflection.isProxyServer() ? (String) Files.TOKEN.getOrSet("token", "insert-token-of-proxy-here") : ConfigSections.Settings.HANDLE_THROUGH_PROXY.TOKEN;
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
            public static CancelCommandSection CANCEL_COMMAND = new CancelCommandSection();
            public static CustomBrandSection CUSTOM_BRAND = new CustomBrandSection();
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
            public static StatsSection STATS_SECTION = new StatsSection();

            public static void initialize() {}
        }
    }
}
