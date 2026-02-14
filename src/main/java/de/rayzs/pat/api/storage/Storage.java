package de.rayzs.pat.api.storage;

import java.util.*;
import java.util.concurrent.TimeUnit;

import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.api.netty.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.api.storage.config.messages.*;
import de.rayzs.pat.api.storage.config.settings.*;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.subarguments.SubArguments;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.group.Group;
import de.rayzs.pat.utils.permission.PermissionPlugin;
import de.rayzs.pat.utils.sender.CommandSender;
import org.bukkit.entity.Player;

import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.storage.blacklist.BlacklistCreator;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralBlacklist;
import de.rayzs.pat.api.storage.blacklist.impl.GeneralIgnoredServers;
import de.rayzs.pat.api.storage.placeholders.commands.general.ListCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.general.ListReversedCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.general.ListSizeCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.general.ListSortedCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.group.ListGroupCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.group.ListGroupReversedCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.group.ListGroupSizeCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.commands.group.ListGroupSortedCommandsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.general.GeneralCurrentVersionPlaceholder;
import de.rayzs.pat.api.storage.placeholders.general.GeneralNewestVersionPlaceholder;
import de.rayzs.pat.api.storage.placeholders.general.GeneralPrefixPlaceholder;
import de.rayzs.pat.api.storage.placeholders.general.GeneralUserPlaceholder;
import de.rayzs.pat.api.storage.placeholders.groups.ListGroupsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.groups.ListGroupsReversedPlaceholder;
import de.rayzs.pat.api.storage.placeholders.groups.ListGroupsSortedPlaceholder;
import de.rayzs.pat.api.storage.placeholders.groups.ListSizeGroupsPlaceholder;
import de.rayzs.pat.api.storage.placeholders.messages.BlockedBaseCommandPlaceholder;
import de.rayzs.pat.api.storage.placeholders.messages.BlockedSubCommandPlaceholder;
import de.rayzs.pat.api.storage.placeholders.messages.UnknownCommandPlaceholder;
import de.rayzs.pat.api.storage.storages.ConfigStorage;
import de.rayzs.pat.api.storage.storages.DisabledServersStorage;
import de.rayzs.pat.api.storage.storages.IgnoredServersStorage;
import de.rayzs.pat.api.storage.storages.PlaceholderStorage;
import de.rayzs.pat.plugin.PluginLoader;
import de.rayzs.pat.utils.ExpireCache;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.ConfigurationBuilder;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.message.replacer.PlaceholderReplacer;
import de.rayzs.pat.utils.permission.PermissionUtil;

public class Storage {

    private static PluginLoader LOADER;

    public static final List<UUID> NOTIFY_PLAYERS = new ArrayList<>();

    public static String TOKEN = "", CENSORED_TOKEN = "", SERVER_NAME = null, CURRENT_VERSION = "", NEWER_VERSION = "";
    public static boolean OUTDATED = false, SEND_CONSOLE_NOTIFICATION = true;
    public static Object PLUGIN_OBJECT;
    public static boolean USE_PLACEHOLDERAPI = false, USE_PAPIPROXYBRIDGE = false, USE_VIAVERSION = false;

    private final static ExpireCache<UUID, String> TEMP_PLAYER_SERVER_CACHE = new ExpireCache<>(1, TimeUnit.SECONDS);
    private static PermissionPlugin PERMISSION_PLUGIN = PermissionPlugin.NONE;

    public static void initialize(PluginLoader loader, String currentVersion) {
        LOADER = loader;
        CURRENT_VERSION = currentVersion;
    }

    public static void setPermissionPlugin(PermissionPlugin permissionPlugin) {
        if (PERMISSION_PLUGIN == PermissionPlugin.LUCKPERMS) {
            return;
        }

        PERMISSION_PLUGIN = permissionPlugin;
    }


    public static void broadcastPermissionsPluginNotice() {
        if (Storage.getPermissionPlugin() != PermissionPlugin.NONE) {
            return;
        }

        Logger.warning("No known permissions plugin found, so some features may be disabled.");
        Logger.warning("Read more about it here: https://rayzs.de/products/proantitab/nkppf");
    }

    public static PermissionPlugin getPermissionPlugin() {
        return PERMISSION_PLUGIN;
    }

    public static void loadAll(boolean loadBlacklist) {
        loadConfig();
        loadToken();

        if (TOKEN != null && !TOKEN.isEmpty()) {
            int tokenLength = TOKEN.length(), cutIndex = Math.max(1, tokenLength - 2);
            CENSORED_TOKEN = TOKEN.charAt(0) + "*".repeat(cutIndex) + TOKEN.substring(cutIndex);
            Logger.info("Token found! (" + CENSORED_TOKEN + ")");
        }

        if (loadBlacklist) Blacklist.loadAll();

        PATEventHandler.callUpdatePluginEvents();
    }

    public static void loadToken() {
        Files.TOKEN.reload();

        boolean invalidEnv = false;

        if (Reflection.isProxyServer()) {
            boolean loadFromEnv = (Boolean) Files.TOKEN.getOrSet("load-from-env.enabled", false);
            String envVariable = (String) Files.TOKEN.getOrSet("load-from-env.name", "PAT_TOKEN");

            if (loadFromEnv) {
                Logger.info("Looking up token from environment variable: " + envVariable);
                TOKEN = System.getenv(envVariable);

                if (TOKEN != null) {
                    return;
                } else invalidEnv = true;
            }

            TOKEN = (String) Files.TOKEN.getOrSet("token", UUID.randomUUID().toString());

            if (invalidEnv)
                Logger.warning("Environment variable not found! Using default Token from the token.yml instead.");

            return;
        }

        if (!ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            return;
        }

        boolean loadFromEnv = ConfigSections.Settings.HANDLE_THROUGH_PROXY.LOAD_FROM_ENV;
        String envVariable = ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENV_NAME;

        if (loadFromEnv) {
            TOKEN = System.getenv(envVariable);

            if (TOKEN != null) {
                return;
            } else invalidEnv = true;

        }

        TOKEN = ConfigSections.Settings.HANDLE_THROUGH_PROXY.TOKEN;

        if (invalidEnv)
            Logger.warning("Environment variable not found! Using default Token from the config.yml instead.");
    }

    public static void loadConfig() {
        ConfigSections.Settings.initialize();
        ConfigSections.Messages.initialize();
        ConfigSections.SECTIONS.forEach(ConfigStorage::load);

        if (USE_PLACEHOLDERAPI)
            ConfigSections.PLACEHOLDERS.forEach(PlaceholderStorage::load);
    }

    public static void reload() {
        boolean proxy = Reflection.isProxyServer();
        boolean backend = Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED && !proxy;

        ConfigUpdater.initialize();
        Storage.loadAll(Reflection.isProxyServer() || !backend);

        CustomServerBrand.initialize();
        GroupManager.clearAllGroups();
        GroupManager.initialize();

        if (!proxy) {
            Communicator.get().reload();
            BackendUpdater.restart();
        }

        if (!backend) {
            Storage.handleChange();
        }

        if (proxy) {
            Communicator.Proxy2Backend.sendDataSync();
        }

        ConfigUpdater.broadcastMissingParts();
        Storage.getLoader().handleReload();
    }

    public static void handleChange() {
        handleChange(null);
    }

    public static void handleChange(String server) {

        if (Reflection.isProxyServer()) {

            boolean isVelocity = Reflection.isVelocityServer();

            if (server != null) {

                Storage.Blacklist.clearServerBlacklists(server);
                Storage.Blacklist.loadCachedServerBlacklists(server);

                List<String> associatedServers = getServers()
                        .stream()
                        .filter(s -> !s.equals(server) && isServer(server, s))
                        .toList();

                associatedServers.forEach(s -> {
                    Storage.Blacklist.clearServerBlacklists(s);
                    Storage.Blacklist.loadCachedServerBlacklists(s);
                });

                if (isVelocity) {

                    associatedServers.forEach(s -> {
                        List<UUID> playerIds = Storage.getLoader().getPlayerIdsByServer(s);
                        List<String> commands = new ArrayList<>(SubArguments.getServerCommands(s));

                        playerIds.forEach(playerId -> {
                            List<String> playerCommands = new ArrayList<>(commands);
                            playerCommands.addAll(SubArguments.getGroupCommands(playerId, s));

                            PATEventHandler.callUpdatePlayerCommandsEvents(playerId, playerCommands, true);
                        });

                    });
                }

            } else {

                Storage.Blacklist.Experimental.forceCacheReset();

                if (isVelocity) {
                    List<UUID> playerIds = Storage.getLoader().getPlayerIds();

                    playerIds.forEach(playerId -> {
                        List<String> playerCommands = new ArrayList<>(SubArguments.getServerCommands(playerId));
                        playerCommands.addAll(SubArguments.getGroupCommands(playerId));

                        PATEventHandler.callUpdatePlayerCommandsEvents(playerId, playerCommands, false);
                    });
                }

            }

            GroupManager.clearServerGroupBlacklists();

            Storage.getLoader().updateCommandCache();

            if (!Reflection.isVelocityServer()) {
                BungeePacketAnalyzer.sendCommandsPacket();
            }

            Communicator.Proxy2Backend.sendUpdateCommand();
            return;
        }

        getLoader().handleReload();
        PermissionUtil.reloadPermissions();

        if (Reflection.getMinor() < 13) {
            final List<UUID> playerIds = Storage.getLoader().getPlayerIds();
            final List<String> commands = new ArrayList<>(Blacklist.BLACKLIST.getCommands());

            playerIds.forEach(playerId -> {
                List<String> playerCommands = new ArrayList<>(commands);
                playerCommands.addAll(SubArguments.getGroupCommands(playerId));

                PATEventHandler.callUpdatePlayerCommandsEvents(playerId, playerCommands, true);
            });
        } else {
            BukkitAntiTabListener.handleTabCompletion();
        }
    }

    public static void tempCachePlayerToServer(UUID playerId, String server) {
        TEMP_PLAYER_SERVER_CACHE.put(playerId, server);
    }

    public static void getTempCachedPlayerToServer(UUID playerId, String server) {
        TEMP_PLAYER_SERVER_CACHE.put(playerId, server);
    }

    public static String getCachedPlayerServername(UUID playerId) {
        return TEMP_PLAYER_SERVER_CACHE.get(playerId);
    }

    // Quickly updates all player subarguments.
    public static void quickSubArgumentUpdate(UUID uuid) {
        List<String> playerCommands = new ArrayList<>(SubArguments.getServerCommands(uuid));
        playerCommands.addAll(SubArguments.getGroupCommands(uuid));

        PATEventHandler.callUpdatePlayerCommandsEvents(uuid, playerCommands, false);
    }

    public static PluginLoader getLoader() {
        return LOADER;
    }

    public static List<String> getServers() {
        return getServers(false);
    }

    public static List<String> getServers(boolean includingServerSpecifics) {
        List<String> servers = LOADER.getServerNames();

        if(includingServerSpecifics) {
            servers.addAll(Blacklist.getBlacklists().stream().map(Map.Entry::getKey).toList());
        }

        return servers;
    }

    public static boolean isServer(String targetServers, List<String> servers) {
        for (String originServer : servers)
            if(isServer(originServer, targetServers))
                return true;

        return false;
    }

    public static boolean isServer(String originServer, String targetServer) {
        originServer = originServer.toLowerCase();
        targetServer = targetServer.toLowerCase();

        if (originServer.endsWith("*")) {
            originServer = originServer.substring(0, originServer.length() - 2);
            return targetServer.startsWith(originServer);
        }

        return originServer.equalsIgnoreCase(targetServer);
    }

    public static class Files {
        public static final ConfigurationBuilder
                CONFIGURATION = Configurator.get("config"),
                STORAGE = Configurator.get("storage"),
                PLACEHOLDERS = Configurator.get("placeholders"),
                CUSTOM_RESPONSES = Configurator.get("custom-responses"),
                TOKEN = Configurator.get("token");

        public static void initialize() {}
    }

    public static class ConfigSections {

        public static List<ConfigStorage> SECTIONS = new ArrayList<>();
        public static List<PlaceholderStorage> PLACEHOLDERS = new ArrayList<>();

        public static class Settings {

            public static AllowGroupOverrulingSection ALLOW_GROUP_OVERRULING = new AllowGroupOverrulingSection();
            public static AutoLowercaseCommandsSection AUTO_LOWERCASE_COMMANDS = new AutoLowercaseCommandsSection();
            public static BlockNamespaceCommandsSection BLOCK_NAMESPACE_COMMANDS = new BlockNamespaceCommandsSection();
            public static HandleThroughProxySection HANDLE_THROUGH_PROXY = new HandleThroughProxySection();
            public static PatchExploitSection PATCH_EXPLOITS = new PatchExploitSection();
            public static CustomBrandSection CUSTOM_BRAND = new CustomBrandSection();
            public static CancelCommandSection CANCEL_COMMAND = new CancelCommandSection();
            public static CustomPluginsSection CUSTOM_PLUGIN = new CustomPluginsSection();
            public static CustomVersionSection CUSTOM_VERSION = new CustomVersionSection();
            public static DisableSyncSection DISABLE_SYNC = new DisableSyncSection();
            public static ForwardConsoleNotificationAlertsSection FORWARD_CONSOLE_NOTIFICATIONS = new ForwardConsoleNotificationAlertsSection();
            public static CustomProtocolPingSection CUSTOM_PROTOCOL_PING = new CustomProtocolPingSection();
            public static CustomUnknownCommandSection CUSTOM_UNKNOWN_COMMAND = new CustomUnknownCommandSection();
            public static TurnBlacklistToWhitelistSection TURN_BLACKLIST_TO_WHITELIST = new TurnBlacklistToWhitelistSection();
            public static BaseCommandCaseSensitiveSection BASE_COMMAND_CASE_SENSITIVE = new BaseCommandCaseSensitiveSection();
            public static UpdateGroupsPerWorldSection UPDATE_GROUPS_PER_WORLD = new UpdateGroupsPerWorldSection();
            public static UpdateGroupsPerServerSection UPDATE_GROUPS_PER_SERVER = new UpdateGroupsPerServerSection();

            public static UpdateSection UPDATE = new UpdateSection();

            public static void initialize() {}
        }

        public static class Messages {

            public static PrefixSection PREFIX = new PrefixSection();
            public static BlacklistSection BLACKLIST = new BlacklistSection();
            public static CommandFailedSection COMMAND_FAILED = new CommandFailedSection();
            public static ConvertSection CONVERT = new ConvertSection();
            public static ExtractSection EXTRACT = new ExtractSection();
            public static GroupSection GROUP = new GroupSection();
            public static HelpSection HELP = new HelpSection();
            public static InfoSection INFO = new InfoSection();
            public static NoPermissionSection NO_PERMISSION = new NoPermissionSection();
            public static NotificationSection NOTIFICATION = new NotificationSection();
            public static PermsCheckSection PERMS_CHECK = new PermsCheckSection();
            public static PostDebugSection POST_DEBUG = new PostDebugSection();
            public static OnlyForProxySection NO_PROXY = new OnlyForProxySection();
            public static ReloadSection RELOAD = new ReloadSection();
            public static ServerListSection SERV_LIST = new ServerListSection();
            public static StatsSection STATS = new StatsSection();
            public static UpdatePermissionsSection UPDATE_PERMISSIONS = new UpdatePermissionsSection();

            public static void initialize() {}
        }

        public static class Placeholders {

            public static GeneralPrefixPlaceholder PREFIX = new GeneralPrefixPlaceholder();
            public static GeneralUserPlaceholder USER = new GeneralUserPlaceholder();
            public static GeneralCurrentVersionPlaceholder CURRENT_VERSION = new GeneralCurrentVersionPlaceholder();
            public static GeneralNewestVersionPlaceholder NEWEST_VERSION = new GeneralNewestVersionPlaceholder();

            public static BlockedBaseCommandPlaceholder BLOCKED_BASE_COMMAND = new BlockedBaseCommandPlaceholder();
            public static BlockedSubCommandPlaceholder BLOCKED_SUB_COMMAND = new BlockedSubCommandPlaceholder();
            public static UnknownCommandPlaceholder UNKNOWN_COMMAND = new UnknownCommandPlaceholder();

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

                if(USE_PLACEHOLDERAPI) {
                    for (PlaceholderStorage storage : PLACEHOLDERS) {
                        if (!storage.getRequest().startsWith(request)) continue;

                        if (storage.getRequest().endsWith("group_") && request.contains("group_"))
                            param = request.split("group_")[1];

                        result = storage.onRequest(player, param);
                        if (result == null) continue;
                        else result = result.replace("\\n", "\n");

                        break;
                    }

                    // Second time because of nested placeholders
                    if (result != null && result.contains("%")) {
                        result = PlaceholderReplacer.replace(player, result);
                    }
                }

                return result;
            }
        }
    }

    public static class Blacklist {

        public static class Experimental {

            public static void forceCacheReset() {
                for (Object o : CACHED_SERVER_BLACKLIST.getCache().asMap().keySet()) {
                    String s = (String) o;

                    clearServerBlacklists(s);
                    loadCachedServerBlacklists(s);
                }
            }

        }

        public enum BlockType {
            CHAT("[CMD]"),
            TAB("[TAB]"),
            NEGATE("!"),

            BOTH("");

            private final String text;

            BlockType(String text) {
                this.text = text;
            }

            @Override
            public String toString() {
                return this.text;
            }
        }

        private static final BlockType[] BLOCK_TYPES = BlockType.values();

        private static final GeneralBlacklist BLACKLIST = BlacklistCreator.createGeneralBlacklist();
        private static final IgnoredServersStorage IGNORED_SERVERS = new GeneralIgnoredServers();
        private static final DisabledServersStorage DISABLED_SERVERS = new DisabledServersStorage();

        // General server-specific list (e.g: server-*)
        private static final HashMap<String, GeneralBlacklist> SERVER_BLACKLISTS = new HashMap<>();
        // Temporary list for all servers (e.g: server-1)
        private static final ExpireCache<String, List<GeneralBlacklist>> CACHED_SERVER_BLACKLIST = new ExpireCache<>(1, TimeUnit.HOURS);

        public static void loadAll() {
            CACHED_SERVER_BLACKLIST.clear();
            SERVER_BLACKLISTS.clear();
            BLACKLIST.load();

            if(!Reflection.isProxyServer())
                return;

            IGNORED_SERVERS.load();
            DISABLED_SERVERS.load();

            BLACKLIST.getConfig().getKeys("global.servers", true).forEach(key -> {
                GeneralBlacklist blacklist = BlacklistCreator.createGeneralBlacklist(key);
                blacklist.load();
                SERVER_BLACKLISTS.put(key, blacklist);
            });

            getServers().forEach(Blacklist::loadCachedServerBlacklists);
        }

        private static void loadCachedServerBlacklists(String server) {
            List<GeneralBlacklist> blacklists = SERVER_BLACKLISTS.entrySet().stream()
                    .filter(entry ->
                            isServer(entry.getKey(), server)
                    ).map(Map.Entry::getValue).toList();

            CACHED_SERVER_BLACKLIST.put(server, blacklists);
        }

        public static GeneralBlacklist getServerBlacklist(String server) {
            GeneralBlacklist blacklist = SERVER_BLACKLISTS.get(server);
            if(blacklist != null) {
                return blacklist;
            }

            blacklist = BlacklistCreator.createGeneralBlacklist(server);
            blacklist.load();

            SERVER_BLACKLISTS.put(server, blacklist);
            return blacklist;
        }

        public static Set<Map.Entry<String, GeneralBlacklist>> getBlacklists() {
            return SERVER_BLACKLISTS.entrySet();
        }

        public static GeneralBlacklist getBlacklist() {
            return BLACKLIST;
        }

        public static List<GeneralBlacklist> getServerBlacklists(String server) {
            if (!CACHED_SERVER_BLACKLIST.contains(server)) {
                loadCachedServerBlacklists(server);
            }

            return CACHED_SERVER_BLACKLIST.get(server);
        }

        public static void clearServerBlacklists(String server) {
            CACHED_SERVER_BLACKLIST.remove(server);
        }

        public static boolean isDisabledServer(String server) {
            return DISABLED_SERVERS.isListed(server);
        }

        public static boolean isIgnoredServer(String server) {
            return IGNORED_SERVERS.isListed(server);
        }

        public static boolean canPlayerAccess(CommandSender sender, List<Group> groups, String command, BlockType type) {
            return canPlayerAccess(sender, groups, command, type, null);
        }

        public static boolean canPlayerAccessChat(CommandSender sender, List<Group> groups, String command) {
            return canPlayerAccess(sender, groups, command, BlockType.CHAT, null);
        }


        public static boolean canPlayerAccessChat(CommandSender sender, List<Group> groups, String command, String server) {
            return canPlayerAccess(sender, groups, command, BlockType.CHAT, server);
        }

        public static boolean canPlayerAccessTab(CommandSender sender, List<Group> groups, String command) {
            return canPlayerAccess(sender, groups, command, BlockType.TAB, null);
        }

        public static boolean canPlayerAccessTab(CommandSender sender, List<Group> groups, String command, String server) {
            return canPlayerAccess(sender, groups, command, BlockType.TAB, server);
        }

        public static boolean canPlayerAccess(CommandSender sender, List<Group> groups, String command, BlockType type, String server) {
            final boolean allowGroupOverruling = ConfigSections.Settings.ALLOW_GROUP_OVERRULING.ENABLED;
            final boolean blocked = isBlocked(command, type, server);

            if (allowGroupOverruling) {
                GroupManager.AccessResult groupResult = GroupManager.canAccessCommand(groups, command, type, server);
                if (groupResult == GroupManager.AccessResult.NEGATED) {
                    return false;
                }

                if (!blocked) {
                    return true;
                }

                if (Storage.getPermissionPlugin() != PermissionPlugin.NONE) {
                    if (PermissionUtil.hasBypassPermission(sender, command)) {
                        return true;
                    }
                }

                return groupResult.asBoolean();
            }


            if (!blocked) {
                return true;
            }

            if (Storage.getPermissionPlugin() != PermissionPlugin.NONE) {
                if (PermissionUtil.hasBypassPermission(sender, command)) {
                    return true;
                }
            }

            return GroupManager.canAccessCommand(groups, command, type, server).asBoolean();
        }

        public static boolean isBlockedChat(String command) {
            return isBlocked(command, BlockType.CHAT, null);
        }

        public static boolean isBlockedTab(String command) {
            return isBlocked(command, BlockType.TAB, null);
        }

        public static boolean isBlockedChat(String command, String server) {
            return isBlocked(command, BlockType.CHAT, server);
        }

        public static boolean isBlockedTab(String command, String server) {
            return isBlocked(command, BlockType.TAB, server);
        }

        public static boolean isBlocked(String command, BlockType type) {
            return isBlocked(command, type, null);
        }

        public static boolean isBlocked(String command, BlockType type, String server) {

            BlockType negation = BlockType.NEGATE;
            String negatedCommand = negation + command;

            boolean all = isListed("*", type, server),
                    listed = isListed(command, type, server),
                    negatedListed = isListed(negatedCommand, type, server),
                    turn = ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;

            if (all) {
                // Only listens to negated commands
                if (negatedListed) {
                    return turn;
                }

                return !turn;
            }

            return turn != listed;

        }

        private static boolean isListed(String unmodifiedCommand, BlockType type, String server) {
            if (server != null && isDisabledServer(server)) {
                return false;
            }

            boolean listed = false;

            String command = type.toString() + unmodifiedCommand;

            if (server == null || !isIgnoredServer(server)) {
                listed = BLACKLIST.isListed(command, false);
            }

            if (server == null) {

                if (!listed && type != BlockType.BOTH) {
                    return isListed(unmodifiedCommand, BlockType.BOTH, null);
                }

                return listed;
            }

            List<GeneralBlacklist> blacklists = new ArrayList<>(getServerBlacklists(server));
            if (blacklists.isEmpty())
                blacklists.add(BLACKLIST);

            for (GeneralBlacklist blacklist : blacklists) {
                if (listed) {
                    break;
                }

                listed = blacklist.isListed(command, false);
            }

            if (!listed && type != BlockType.BOTH) {
                return isListed(unmodifiedCommand, BlockType.BOTH, server);
            }

            return listed;
        }

        public static class BlockTypeFetcher {

            public static boolean isNegated(String command) {
                if (command.isEmpty()) {
                    return false;
                }

                boolean negated = command.charAt(0) == '!';

                if (!negated && command.length() > 5)
                    negated = command.charAt(5) == '!';

                return negated;
            }

            public static BlockType getType(String command) {
                for (BlockType blockType : BLOCK_TYPES) {
                    if (!command.startsWith(blockType.toString()))
                        continue;

                    return blockType;
                }

                return null;
            }

            public static String modify(String command) {
                BlockType type = getType(command);
                return modify(command, type);
            }

            public static String modify(String command, BlockType type) {
                if (type == null || type == BlockType.BOTH || command.length() <= type.toString().length() || !command.startsWith(type.toString()))
                    return command;

                return command.substring(type.toString().length());
            }

            // Remove all BlockType elements from String
            public static String cleanse(String command) {
                BlockType type = getType(command);

                if (type == null || type == BlockType.BOTH) {
                    return command;
                }

                return modify(command, type);
            }

        }
    }
}