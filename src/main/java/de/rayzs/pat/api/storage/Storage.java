package de.rayzs.pat.api.storage;

import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.config.messages.OnlyForProxySection;
import de.rayzs.pat.api.storage.config.messages.StatsSection;
import de.rayzs.pat.api.storage.config.settings.*;
import de.rayzs.pat.api.storage.templates.ConfigStorage;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.Configurator;

import java.util.ArrayList;
import java.util.List;

public class Storage {

    public static final GeneralBlacklist BLACKLIST = BlacklistCreator.createGeneralBlacklist();

    public static class Files {
        public static final ConfigurationBuilder
                CONFIGURATION = Configurator.get("settings"),
                STORAGE = Configurator.get("blacklist"),
                TOKEN = Configurator.get("token");
    }

    public static class ConfigSections {

        public static List<ConfigStorage> LIST = new ArrayList<>();

        public static class Messages {
            public static OnlyForProxySection NO_PROXY = new OnlyForProxySection();
            public static StatsSection STATS_SECTION = new StatsSection();

            public static void initialize() {}
        }

        public static class Settings {
            public static CancelCommandSection CANCEL_COMMAND = new CancelCommandSection();
            public static CustomBrandSection CUSTOM_BRAND = new CustomBrandSection();
            public static CustomPluginsSection CUSTOM_PLUGIN = new CustomPluginsSection();
            public static CustomUnknownCommandSection CUSTOM_UNKNOWN_COMMAND = new CustomUnknownCommandSection();
            public static UpdateSection UPDATE = new UpdateSection();

            public static void initialize() {}
        }

        public static void loadAll() {
            Messages.initialize();
            Settings.initialize();

            LIST.forEach(ConfigStorage::load);
        }
    }
}
