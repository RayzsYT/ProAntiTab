package de.rayzs.pat.plugin;

import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.plugin.converter.StorageConverter;
import de.rayzs.pat.plugin.subarguments.SubArguments;
import de.rayzs.pat.plugin.process.CommandProcess;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.utils.adapter.GroupManagerAdapter;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.group.TinyGroup;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.adapter.ViaVersionAdapter;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.plugin.commands.BukkitCommand;
import de.rayzs.pat.utils.hooks.PlaceholderHook;
import de.rayzs.pat.plugin.listeners.bukkit.*;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.response.action.ActionHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.scheduler.*;
import org.bukkit.entity.Player;
import java.lang.reflect.Field;
import de.rayzs.pat.utils.*;
import org.bukkit.command.*;
import org.bukkit.plugin.*;
import org.bukkit.Bukkit;
import java.util.*;
import java.util.stream.Collectors;

public class BukkitLoader extends JavaPlugin implements PluginLoader {

    private static List<String> commands = new ArrayList<>(),
            allowedCommands = new ArrayList<>(),
            disallowedCommands = new ArrayList<>();

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private static boolean loaded = false, suggestions = false;
    private static Map<String, Command> commandsMap = null;
    private PATSchedulerTask updaterTask;

    private final List<String> offlinePlayerNames = new ArrayList<>();
    private long lastCommandsLoad = -1;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile("files\\bukkit-config.yml", "config.yml", false);
        Configurator.createResourcedFile("files\\bukkit-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile("files\\bukkit-placeholders.yml", "placeholders.yml", false);
        Configurator.createResourcedFile("files\\bukkit-custom-responses.yml", "custom-responses.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        for (OfflinePlayer offlinePlayerName : Bukkit.getOfflinePlayers()) {
            offlinePlayerNames.add(offlinePlayerName.getName());
        }

        loadCommandMap();

        CommandProcess.initialize();
        Reflection.initialize(getServer());
        ConfigUpdater.initialize();

        Storage.initialize(this, getDescription().getVersion());
        VersionComparer.get().setCurrentVersion(Storage.CURRENT_VERSION);

        Storage.loadAll(true);

        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            new PlaceholderHook().register();

        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        bStats.initialize(this);

        PluginManager manager = getServer().getPluginManager();

        if (!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            loaded = true;
            GroupManager.initialize();
            BukkitPacketAnalyzer.injectAll();
        } else BackendUpdater.handle();

        manager.registerEvents(new BukkitPlayerListener(), this);
        manager.registerEvents(new BukkitBlockCommandListener(), this);

        if (Reflection.getMinor() >= 13) {
            suggestions = true;
            manager.registerEvents(new BukkitAntiTabListener(), this);
        }

        if (Reflection.isPaper() && Reflection.getMinor() >= 12)
            manager.registerEvents(new PaperServerListPing(), this);

        registerCommand("proantitab", "pat");
        startUpdaterTask();

        Storage.PLUGIN_OBJECT = this;

        if (getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            LuckPermsAdapter.initialize();
            Bukkit.getOnlinePlayers().forEach(player -> PermissionUtil.setPlayerPermissions(player.getUniqueId()));
        } else {
            final Plugin groupManagerPlugin = getServer().getPluginManager().getPlugin("GroupManager");
            if (groupManagerPlugin != null) {
                GroupManagerAdapter.initialize(groupManagerPlugin);
                Bukkit.getOnlinePlayers().forEach(player -> PermissionUtil.setPlayerPermissions(player.getUniqueId()));
            }
        }

        if (getServer().getPluginManager().getPlugin("ViaVersion") != null)
            ViaVersionAdapter.initialize();

        if (Storage.USE_SIMPLECLOUD) {
            Logger.warning("Detected SimpleCloud. Therefore, MiniMessages are disabled!");
        }

        Storage.broadcastPermissionsPluginNotice();
        ConfigUpdater.broadcastMissingParts();

        ActionHandler.initialize();
        SubArguments.initialize();

        StorageConverter.initialize();

        PATScheduler.createScheduler(() -> {
            Storage.reload();
            loadAllCommands();
        });
    }

    @Override
    public void onDisable() {
        BackendUpdater.stop();
        BukkitPacketAnalyzer.uninjectAll();
        MessageTranslator.closeAudiences();
    }

    public void registerCommand(String... commands) {
        BukkitCommand command = new BukkitCommand();
        for (String commandName : commands) {
            PluginCommand pluginCommand = getCommand(commandName);
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    @Override
    public void handleReload() {
        loadAllCommands();
    }

    @Override
    public boolean doesCommandExist(String command) {
        if (commandsMap == null)
            return false;

        loadAllCommands();

        return getAllCommands().contains(command);
    }

    @Override
    public Object getConsoleSender() {
        return Bukkit.getConsoleSender();
    }

    @Override
    public Object getPlayerObjByName(String name) {
        return Bukkit.getPlayer(name);
    }

    @Override
    public Object getPlayerObjByUUID(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    @Override
    public void updateCommandCache() {}

    @Override
    public HashMap<String, CommandsCache> getCommandsCacheMap() {
        return null;
    }

    @Override
    public boolean isPlayerOnline(String playerName) {
        return Bukkit.getPlayer(playerName) != null;
    }

    @Override
    public boolean doesPlayerExist(String playerName) {
        return isPlayerOnline(playerName) || ArrayUtils.containsIgnoreCase(getPlayerNames(), playerName);
    }

    @Override
    public String getPlayerServerName(UUID uuid) {
        return null;
    }

    @Override
    public List<UUID> getPlayerIdsByServer(String server) {
        return null;
    }

    @Override
    public List<String> getOnlinePlayerNames(String serverName) {
        return getOnlinePlayerNames();
    }

    @Override
    public List<UUID> getPlayerIds() {
        return Bukkit.getOnlinePlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList());
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return new ArrayList<>(Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList());
    }

    @Override
    public List<String> getOfflinePlayerNames() {
        return offlinePlayerNames;
    }

    @Override
    public List<String> getPlayerNames() {
        List<String> playerNames = new ArrayList<>(offlinePlayerNames);

        playerNames.addAll(getOnlinePlayerNames());

        return playerNames;
    }

    @Override
    public String getNameByUUID(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? player.getName() : "";
    }

    @Override
    public UUID getUUIDByName(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public List<String> getServerNames() {
        return List.of();
    }

    @Override
    public void delayedPermissionsReload() {
        PATScheduler.createScheduler(PermissionUtil::reloadPermissions, 40);
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED)
            return;

        updaterTask = PATScheduler.createAsyncScheduler(() -> {

            if (VersionComparer.get().computeComparison())
                updaterTask.cancelTask();

        }, 20L, 20L * Storage.ConfigSections.Settings.UPDATE.PERIOD);
    }

    public static void handleNotificationPacket(CommunicationPackets.NotificationPacket packet) {
        if (!Storage.SEND_CONSOLE_NOTIFICATION) { return; }

        final Player player = Bukkit.getPlayer(packet.getTargetUUID());
        if (player == null) {
            return;
        }

        final List<String> notificationMessage = MessageTranslator.replaceMessageList(
                Storage.ConfigSections.Messages.NOTIFICATION.ALERT,
                "%player%", player.getName(),
                "%command%", packet.getDisplayedCommand(),
                "%server%", Storage.SERVER_NAME,
                "%world%", player.getWorld().getName());

        Logger.info(notificationMessage);
    }

    public static void handleUpdateCommandsPacket(CommunicationPackets.UpdateCommandsPacket packet) {

        if (!packet.hasTargetUUID()) {
            Bukkit.getOnlinePlayers().forEach(Player::updateCommands);
            return;
        }

        Player player = Bukkit.getPlayer(packet.getTargetUUID());
        if (player != null && Reflection.getMinor() >= 13) {
            player.updateCommands();
        }
    }

    public static void synchronize(CommunicationPackets.PacketBundle packetBundle) {
        Storage.LAST_SYNC = System.currentTimeMillis();
        Communicator.sendFeedback();

        CommunicationPackets.UnknownCommandPacket unknownCommandPacket = packetBundle.getUnknownCommandPacket();
        CommunicationPackets.CommandsPacket commandsPacket = packetBundle.getCommandsPacket();
        CommunicationPackets.GroupsPacket groupsPacket = packetBundle.getGroupsPacket();
        CommunicationPackets.NamespaceCommandsPacket namespaceCommandsPacket = packetBundle.getNamespaceCommandsPacket();
        CommunicationPackets.MessagePacket messagePacket = packetBundle.getMessagePacket();

        boolean updatedList = false;

        if (!messagePacket.getBaseBlockedMessage().getLines().isEmpty())
            Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE = messagePacket.getBaseBlockedMessage();

        if (!messagePacket.getSubBlockedMessage().getLines().isEmpty())
            Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE = messagePacket.getSubBlockedMessage();

        if (!messagePacket.getPrefix().isEmpty())
            Storage.ConfigSections.Messages.PREFIX.PREFIX = messagePacket.getPrefix();

        if (Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.ENABLED != namespaceCommandsPacket.isEnabled())
            Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.ENABLED = namespaceCommandsPacket.isEnabled();

        if (commandsPacket.getCommands() == null || commandsPacket.getCommands().isEmpty())
            Storage.Blacklist.getBlacklist().setList(new ArrayList<>());

        else if (!ArrayUtils.compareStringArrays(Storage.Blacklist.getBlacklist().getCommands(), commandsPacket.getCommands())) {
            updatedList = true;
            Storage.Blacklist.getBlacklist().setList(commandsPacket.getCommands());
        }

        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED != commandsPacket.turnBlacklistToWhitelistEnabled())
            Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED = commandsPacket.turnBlacklistToWhitelistEnabled();

        Map<String, List<String>> cpyCache = new HashMap<>();
        GroupManager.getGroups().forEach(group -> {
            cpyCache.put(group.getGroupName(), group.getCommands());
        });

        GroupManager.clearAllGroups();

        for (TinyGroup group : groupsPacket.getGroups()) {
            List<String> currentExistingList = cpyCache.get(group.getGroupName());
            if (
                    !updatedList
                    && currentExistingList != null
                    && !ArrayUtils.compareStringArrays(currentExistingList, group.getCommands())
            ) {
                updatedList = true;
            }

            GroupManager.setGroup(group.getGroupName(), group.getPriority(), group.getCommands());
        }

        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE = unknownCommandPacket.getMessage();
        if (Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED != unknownCommandPacket.isEnabled())
            Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED = unknownCommandPacket.isEnabled();

        if (!loaded) {
            loaded = true;
        }

        if (Reflection.getMinor() >= 13 && updatedList) {
            BukkitAntiTabListener.updateCommands();
        }

        PATEventHandler.callReceiveSyncEvents(packetBundle);
    }

    public static List<String> getAllCommands() {
        return new ArrayList<>(commands);
    }

    public static List<String> getAllowedCommands() {
        return new ArrayList<>(allowedCommands);
    }

    public static List<String> getDisallowedCommands() {
        return disallowedCommands;
    }

    @Override
    public List<String> getPluginNames(String format) {
        List<String> pluginNames = new ArrayList<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            PluginDescriptionFile description = plugin.getDescription();

            pluginNames.add(
                    format.replace("%n", description.getName())
                            .replace("%v", description.getVersion())
            );
        }

        return pluginNames;
    }

    @Override
    public List<String> getAllCommands(boolean useColons) {
        List<String> commands = new ArrayList<>();

        for (Map.Entry<String, Command> entry : BukkitLoader.getCommandsMap().entrySet()) {
            if (entry.getKey().toLowerCase().contains(":")) {
                String command = entry.getKey().substring(entry.getKey().lastIndexOf(":") + 1);

                commands.add(command);

                if (useColons) {
                    commands.add(entry.getKey());
                }
            }
        }

        return commands;
    }

    @Override
    public List<String> getPluginCommands(String pluginName, boolean useColons) {
        List<String> commands = new ArrayList<>();

        if (pluginName.isBlank()) return commands;

        pluginName = pluginName.toLowerCase();

        for (Map.Entry<String, Command> entry : BukkitLoader.getCommandsMap().entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(pluginName + ":")) {
                String command = entry.getKey().substring(pluginName.length() + 1);

                commands.add(command);

                if (useColons) {
                    commands.add(entry.getKey());
                }
            }
        }

        return commands;
    }

    private void loadCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                SimpleCommandMap simpleCommandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commandsMap = (Map<String, Command>) knownCommandsField.get(simpleCommandMap);
            }
        } catch (Throwable ignored) { }

        if (commandsMap == null) {
            Logger.warning("Failed to get server commands!");
        }
    }

    private void loadAllCommands() {

        if (lastCommandsLoad != -1 && System.currentTimeMillis() - lastCommandsLoad < 1000)
            return;

        List<String> result = new ArrayList<>();

        if (commandsMap == null) {
            result.addAll(
                    Bukkit.getHelpMap().getHelpTopics().stream()
                    .map(topic -> {
                        String name = topic.getName();

                        if (name.startsWith("/"))
                            name = name.substring(1);

                        return name;
                    }).toList()
            );

            return;
        }

        commandsMap.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Command command = entry.getValue();

            result.add(key);

            if (!command.getAliases().isEmpty()) {
                result.addAll(command.getAliases());
            }
        });

        boolean turn = Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED;
        List<String> allowedCommands = new ArrayList<>(result).stream().filter(command -> {
            boolean contains = Storage.Blacklist.getBlacklist().getCommands().contains(command);
            return turn == contains;
        }).toList();

        lastCommandsLoad = System.currentTimeMillis();

        BukkitLoader.commands = result;
        BukkitLoader.allowedCommands = allowedCommands;
        BukkitLoader.disallowedCommands = commands.stream().filter(command -> !allowedCommands.contains(command)).toList();
    }

    public static Map<String, Command> getCommandsMap() {
        return commandsMap;
    }

    public static boolean useSuggestions() {
        return suggestions;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static java.util.logging.Logger getPluginLogger() {
        return logger;
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
