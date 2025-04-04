package de.rayzs.pat.plugin;

import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.adapter.ViaVersionAdapter;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.plugin.commands.BukkitCommand;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.utils.hooks.PlaceholderHook;
import de.rayzs.pat.plugin.listeners.bukkit.*;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.response.action.ActionHandler;
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

public class BukkitLoader extends JavaPlugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private static boolean loaded = false, checkUpdate = false, suggestions = false;
    private static Map<String, Command> commandsMap = null;
    private PATSchedulerTask updaterTask;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-config.yml", "config.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-placeholders.yml", "placeholders.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-custom-responses.yml", "custom-responses.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        loadCommandMap();

        Reflection.initialize(getServer());
        ConfigUpdater.initialize();

        Storage.CURRENT_VERSION = getDescription().getVersion();
        VersionComparer.setCurrentVersion(Storage.CURRENT_VERSION);

        Storage.loadAll(true);

        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            new PlaceholderHook().register();

        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        bStats.initialize(this);

        PluginManager manager = getServer().getPluginManager();

        if(!Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) {
            loaded = true;
            GroupManager.initialize();
            BukkitPacketAnalyzer.injectAll();
        } else BackendUpdater.handle();

        manager.registerEvents(new BukkitPlayerConnectionListener(), this);
        manager.registerEvents(new BukkitBlockCommandListener(), this);
        if(Reflection.getMinor() >= 13) {
            suggestions = true;
            manager.registerEvents(new BukkitAntiTabListener(), this);
        }

        if(Reflection.isPaper() && Reflection.getMinor() >= 12)
            manager.registerEvents(new PaperServerListPing(), this);

        registerCommand("proantitab", "pat");
        startUpdaterTask();

        Storage.PLUGIN_OBJECT = this;

        if(getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            LuckPermsAdapter.initialize();
            Bukkit.getOnlinePlayers().forEach(player -> PermissionUtil.setPlayerPermissions(player.getUniqueId()));
        }

        if(getServer().getPluginManager().getPlugin("ViaVersion") != null)
            ViaVersionAdapter.initialize();

        if(Storage.USE_SIMPLECLOUD)
            Logger.warning("Detected SimpleCloud. Therefore, MiniMessages are disabled!");

        ConfigUpdater.broadcastMissingParts();
        ActionHandler.initialize();
        SubArgsModule.initialize();
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

    public static List<String> getPlayerNames() {
        List<String> result = new LinkedList<>();
        Bukkit.getServer().getOnlinePlayers().forEach(player -> result.add(player.getName()));
        return result;
    }

    public static UUID getUUIDByName(String playerName) {
        Player player = Bukkit.getServer().getPlayer(playerName);
        return player != null ? player.getUniqueId() : null;
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED) return;
        updaterTask = PATScheduler.createAsyncScheduler(() -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();

            if(result == null) result = "internet";
            Storage.NEWER_VERSION = result;
            VersionComparer.setNewestVersion(Storage.NEWER_VERSION);

            if(VersionComparer.isDeveloperVersion()) {
                updaterTask.cancelTask();
                Logger.info("§8[§fPAT | Bukkit§8] §7Please be aware that you are currently using a §bdeveloper §7version of ProAntiTab. Bugs, errors and a lot of debug messages might be included.");

            } else if(!checkUpdate && (VersionComparer.isNewest() || VersionComparer.isUnreleased())) {
                updaterTask.cancelTask();
                checkUpdate = true;

                if(VersionComparer.isUnreleased()) {
                    Logger.info("§8[§fPAT | Bukkit§8] §7Please be aware that you are currently using an §eunreleased §7version of ProAntiTab.");
                    return;
                }

                Storage.ConfigSections.Settings.UPDATE.UPDATED.getLines().forEach(Logger::warning);

            } else if(VersionComparer.isOutdated()) {
                updaterTask.cancelTask();
                Storage.OUTDATED = true;
                Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines().forEach(Logger::warning);

            } else if(!Storage.NEWER_VERSION.equals(Storage.CURRENT_VERSION)) {
                updaterTask.cancelTask();
                switch (result) {
                    case "internet":
                        Logger.warning("Failed to build connection to website! (No internet?)");
                        break;
                    case "unknown":
                        Logger.warning("Failed to build connection to website! (Firewall enabled or website down?)");
                        break;
                    case "exception":
                        Logger.warning("Failed to build connection to website! (Outdated java version?)");
                        break;
                }
            }
        }, 20L, 20L * Storage.ConfigSections.Settings.UPDATE.PERIOD);
    }

    public static void synchronize(CommunicationPackets.PacketBundle packetBundle) {
        Storage.LAST_SYNC = System.currentTimeMillis();
        Communicator.sendFeedback();

        CommunicationPackets.UnknownCommandPacket unknownCommandPacket = packetBundle.getUnknownCommandPacket();
        CommunicationPackets.CommandsPacket commandsPacket = packetBundle.getCommandsPacket();
        CommunicationPackets.GroupsPacket groupsPacket = packetBundle.getGroupsPacket();
        CommunicationPackets.NamespaceCommandsPacket namespaceCommandsPacket = packetBundle.getNamespaceCommandsPacket();
        CommunicationPackets.MessagePacket messagePacket = packetBundle.getMessagePacket();

        if(!messagePacket.getBaseBlockedMessage().getLines().isEmpty())
            Storage.ConfigSections.Settings.CANCEL_COMMAND.BASE_COMMAND_RESPONSE = messagePacket.getBaseBlockedMessage();

        if(!messagePacket.getSubBlockedMessage().getLines().isEmpty())
            Storage.ConfigSections.Settings.CANCEL_COMMAND.SUB_COMMAND_RESPONSE = messagePacket.getSubBlockedMessage();

        if(!messagePacket.getPrefix().isEmpty())
            Storage.ConfigSections.Messages.PREFIX.PREFIX = messagePacket.getPrefix();

        if(Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.ENABLED != namespaceCommandsPacket.isEnabled())
            Storage.ConfigSections.Settings.BLOCK_NAMESPACE_COMMANDS.ENABLED = namespaceCommandsPacket.isEnabled();

        if (commandsPacket.getCommands() == null || commandsPacket.getCommands().isEmpty())
            Storage.Blacklist.getBlacklist().setList(new ArrayList<>());

        else if (!ArrayUtils.compareStringArrays(Storage.Blacklist.getBlacklist().getCommands(), commandsPacket.getCommands())) {
            if (!Storage.USE_VELOCITY && Reflection.getMinor() >= 13) BukkitAntiTabListener.setChangeStatus();
            Storage.Blacklist.getBlacklist().setList(commandsPacket.getCommands());
        }

        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED != commandsPacket.turnBlacklistToWhitelistEnabled())
            Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED = commandsPacket.turnBlacklistToWhitelistEnabled();

        GroupManager.clearAllGroups();
        groupsPacket.getGroups().forEach(group -> GroupManager.setGroup(group.getGroupName(), group.getPriority(), group.getCommands()));

        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE = unknownCommandPacket.getMessage();
        if(Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED != unknownCommandPacket.isEnabled())
            Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED = unknownCommandPacket.isEnabled();

        if(!loaded) loaded = true;

        if(Reflection.getMinor() >= 16) {

            if (Storage.USE_VELOCITY)
                BukkitAntiTabListener.updateCommands();

            Logger.debug("Force server to load all players tab-completion due to received sync!");
            BukkitAntiTabListener.handleTabCompletion(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED ? Storage.Blacklist.getBlacklist().getCommands() : getNotBlockedCommands());
        }

        PATEventHandler.callReceiveSyncEvents(packetBundle);
    }

    public static boolean doesCommandExist(String command, boolean replace) {
        if(commandsMap == null) return false;
        if(replace) command = StringUtils.replaceFirst(command, "/", "");
        return getAllCommands().contains(command);
    }

    private void loadCommandMap() {
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                SimpleCommandMap simpleCommandMap = (SimpleCommandMap) commandMapField.get(Bukkit.getPluginManager());
                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
                commandsMap = (Map<String, Command>)knownCommandsField.get(simpleCommandMap);
            }
        }catch (Throwable ignored) { }

        if(commandsMap == null)
            Logger.warning("Failed to get server commands!");
    }

    public static List<String> getNotBlockedCommands() {
        List<String> commands = new ArrayList<>();
        Bukkit.getHelpMap().getHelpTopics().stream()
                .filter(topic -> !topic.getName().contains(":") && topic.getName().startsWith("/") && !Storage.Blacklist.getBlacklist().isListed(topic.getName().replaceFirst("/", "")))
                .forEach(topic -> commands.add(topic.getName().replaceFirst("/", "")));
        return commands;
    }

    public static List<String> getAllCommands() {
        if(commandsMap == null) return getNotBlockedCommands();

        List<String> result = new LinkedList<>();
        for (String command : commandsMap.keySet()) if(!result.contains(command)) result.add(command);

        if(Reflection.getMinor() == 20 && Reflection.getRelease() >= 5 || Reflection.getMinor() >= 21)
            for (String s : commandsMap.keySet()) {
                if (commandsMap.containsKey(s)) {
                    if (!commandsMap.get(s).getAliases().isEmpty()) {
                        for (String alias : commandsMap.get(s).getAliases()) {
                            if (!result.contains(alias)) result.add(alias);
                        }

                    } else if (Bukkit.getServer().getCommandAliases().get(s) != null)
                        for (String alias : Bukkit.getServer().getCommandAliases().get(s))
                            if (!result.contains(alias)) result.add(alias);
                }
            }

        return result;
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
