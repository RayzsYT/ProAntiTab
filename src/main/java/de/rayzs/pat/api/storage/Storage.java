package de.rayzs.pat.api.storage;

import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.config.messages.*;
import de.rayzs.pat.api.storage.config.settings.*;
import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.Configurator;

import java.util.ArrayList;
import java.util.List;

public class Storage {

    public static final GeneralBlacklist BLACKLIST = BlacklistCreator.createGeneralBlacklist();
    public static String TOKEN, SERVER_NAME;

    public static void loadToken() {

    }

    public static void loadConfig() {
        ConfigSections.Messages.initialize();
        ConfigSections.Settings.initialize();
        ConfigSections.SECTIONS.forEach(ConfigStorage::load);
    }

    public static class Files {
        public static final ConfigurationBuilder
                CONFIGURATION = Configurator.get("settings"),
                STORAGE = Configurator.get("blacklist"),
                TOKEN = Configurator.get("token");

        public static void initialize() {}
    }

    public static class ConfigSections {

        public static List<ConfigStorage> SECTIONS = new ArrayList<>();

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

        public static class Settings {

            public static CancelCommandSection CANCEL_COMMAND = new CancelCommandSection();
            public static CustomBrandSection CUSTOM_BRAND = new CustomBrandSection();
            public static CustomPluginsSection CUSTOM_PLUGIN = new CustomPluginsSection();
            public static CustomUnknownCommandSection CUSTOM_UNKNOWN_COMMAND = new CustomUnknownCommandSection();
            public static HandleThroughProxySection HANDLE_THROUGH_PROXY = new HandleThroughProxySection();
            public static TurnBlacklistToWhitelistSection TURN_BLACKLIST_TO_WHITELIST = new TurnBlacklistToWhitelistSection();
            public static UpdateSection UPDATE = new UpdateSection();

            public static void initialize() {}
        }
    }
}
