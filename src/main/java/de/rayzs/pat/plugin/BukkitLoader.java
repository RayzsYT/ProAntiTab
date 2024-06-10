package de.rayzs.pat.plugin;

import de.rayzs.pat.api.communication.BackendUpdater;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.commands.BukkitCommand;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitBlockCommandListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitPlayerConnectionListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.api.netty.bukkit.BukkitPacketAnalyzer;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.hooks.PlaceholderHook;
import de.rayzs.pat.utils.adapter.ViaVersionAdapter;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.utils.*;

import java.lang.reflect.Field;
import java.util.*;

public class BukkitLoader extends JavaPlugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private static boolean loaded = false, checkUpdate = false;
    private static Map<String, Command> commandsMap = null;
    private int updaterTaskId;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-config.yml", "config.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\placeholders.yml", "placeholders.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getServer());
        Storage.CURRENT_VERSION = getDescription().getVersion();

        Storage.loadAll(true);

        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
            new PlaceholderHook().register();
        ;
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
        if(Reflection.getMinor() >= 16) manager.registerEvents(new BukkitAntiTabListener(), this);

        registerCommand("proantitab", "pat");
        startUpdaterTask();

        Storage.PLUGIN_OBJECT = this;

        if(getServer().getPluginManager().getPlugin("LuckPerms") != null) {
            LuckPermsAdapter.initialize();
            Bukkit.getOnlinePlayers().forEach(player -> PermissionUtil.setPlayerPermissions(player.getUniqueId()));
        }

        if(getServer().getPluginManager().getPlugin("ViaVersion") != null)
            ViaVersionAdapter.initialize();

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
            Logger.warning("Failed injection task! Skript commands won't be detected with that.");
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

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED || Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED) return;
        updaterTaskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();

            if(result == null) result = "internet";
            Storage.NEWER_VERSION = result;

            if (!Storage.NEWER_VERSION.equals(Storage.CURRENT_VERSION)) {
                Bukkit.getScheduler().cancelTask(updaterTaskId);
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
                    default:
                        Storage.OUTDATED = true;
                        Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines().forEach(Logger::warning);
                        break;
                }
            } else {
                if(!checkUpdate) {
                    checkUpdate = true;
                    Storage.ConfigSections.Settings.UPDATE.UPDATED.getLines().forEach(Logger::warning);
                }
            }
        }, 20L, 20L * Storage.ConfigSections.Settings.UPDATE.PERIOD);
    }

    public static void synchronize(CommunicationPackets.PacketBundle packetBundle) {
        Communicator.sendFeedback();
        CommunicationPackets.UnknownCommandPacket unknownCommandPacket = packetBundle.getUnknownCommandPacket();

        if(!Storage.USE_VELOCITY) {
            CommunicationPackets.CommandsPacket commandsPacket = packetBundle.getCommandsPacket();
            CommunicationPackets.GroupsPacket groupsPacket = packetBundle.getGroupsPacket();

            if (commandsPacket.getCommands() == null || commandsPacket.getCommands().isEmpty())
                Storage.Blacklist.getBlacklist().setList(new ArrayList<>());
            else if (!Storage.Blacklist.getBlacklist().getCommands().containsAll(commandsPacket.getCommands()) || !commandsPacket.getCommands().containsAll(Storage.Blacklist.getBlacklist().getCommands()))
                Storage.Blacklist.getBlacklist().setList(commandsPacket.getCommands());

            if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED != commandsPacket.turnBlacklistToWhitelistEnabled())
                Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED = commandsPacket.turnBlacklistToWhitelistEnabled();

            GroupManager.clearAllGroups();
            groupsPacket.getGroups().forEach(group -> GroupManager.setGroup(group.getGroupName(), group.getCommands()));
        }

        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE = unknownCommandPacket.getMessage();
        if(Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED != unknownCommandPacket.isEnabled())
            Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED = unknownCommandPacket.isEnabled();

        if(!loaded) loaded = true;

        if(Reflection.getMinor() < 16) return;

        if(Storage.USE_VELOCITY) {
            BukkitAntiTabListener.updateCommands();
            return;
        }

        BukkitAntiTabListener.handleTabCompletion(Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED ? Storage.Blacklist.getBlacklist().getCommands() : getNotBlockedCommands());
    }

    public static boolean doesCommandExist(String command) {
        if(commandsMap == null) return false;
        if(command.startsWith("/")) command = StringUtils.replaceFirst(command, "/", "");

        for (String currentCommand : commandsMap.keySet())
            if (currentCommand.equals(command)) return true;

        return false;
    }

    public static List<String> getNotBlockedCommands() {
        List<String> commands = new ArrayList<>();
        Bukkit.getHelpMap().getHelpTopics().stream()
                .filter(topic -> !topic.getName().contains(":") && topic.getName().startsWith("/") && !Storage.Blacklist.getBlacklist().isListed(topic.getName().replaceFirst("/", "")))
                .forEach(topic -> commands.add(topic.getName().replaceFirst("/", "")));
        return commands;
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
