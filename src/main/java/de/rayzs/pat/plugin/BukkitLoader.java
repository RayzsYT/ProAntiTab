package de.rayzs.pat.plugin;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.commands.BukkitCommand;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitBlockCommandListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitPlayerConnectionListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.api.netty.PacketAnalyzer;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.ClientCommunication;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.message.MessageTranslator;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.utils.*;

import java.util.*;

public class BukkitLoader extends JavaPlugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private static boolean loaded = false, checkUpdate = false;
    private int updaterTaskId;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-config.yml", "config.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-storage.yml", "storage.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getServer());
        Storage.CURRENT_VERSION = getDescription().getVersion();

        Storage.loadAll(true);

        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) MessageTranslator.enablePlaceholderSupport();
        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        bStats.initialize(this);

        PluginManager manager = getServer().getPluginManager();

        if(Storage.ConfigSections.Settings.HANDLE_THROUGH_PROXY.ENABLED)
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
                if(Bukkit.getOnlinePlayers().size() >= 1 || loaded) {
                    if(loaded && System.currentTimeMillis() - ClientCommunication.LAST_BUKKIT_SYNC >= 12000) {
                        loaded = false;
                    } else ClientCommunication.sendRequest();
                }
            }, Bukkit.getOnlinePlayers().size() >= 1 ? 10 : 40, 20 * 10);

        else {
            GroupManager.initialize();
            PacketAnalyzer.injectAll();
        }

        if(Reflection.getMinor() >= 16) manager.registerEvents(new BukkitAntiTabListener(), this);
        manager.registerEvents(new BukkitPlayerConnectionListener(), this);
        manager.registerEvents(new BukkitBlockCommandListener(), this);

        registerCommand("proantitab", "pat");
        startUpdaterTask();
    }

    @Override
    public void onDisable() {
        PacketAnalyzer.uninjectAll();
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
            Storage.NEWER_VERSION = result;

            if (!Storage.NEWER_VERSION.equals(Storage.CURRENT_VERSION)) {
                Bukkit.getScheduler().cancelTask(updaterTaskId);
                if (result.equals("unknown")) {
                    Logger.warning("Failed reaching web host! (firewall enabled? website down?)");
                } else if (result.equals("exception")) {
                    Logger.warning("Failed creating web instance! (outdated java version?)");
                } else {
                    Storage.OUTDATED = true;
                    Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines().forEach(Logger::warning);
                }
            } else {
                if(!checkUpdate) {
                    checkUpdate = true;
                    Storage.ConfigSections.Settings.UPDATE.UPDATED.getLines().forEach(Logger::warning);
                }
            }
        }, 20L, 20L * Storage.ConfigSections.Settings.UPDATE.PERIOD);
    }

    public static void synchronizeCommandData(DataConverter.CommandsPacket packet, DataConverter.UnknownCommandPacket unknownCommandPacket) {
        ClientCommunication.sendFeedback();

        if(packet.getCommands() == null || packet.getCommands().isEmpty())
            Storage.Blacklist.getBlacklist().setList(new ArrayList<>());
        else if (!Storage.Blacklist.getBlacklist().getCommands().containsAll(packet.getCommands()) || !packet.getCommands().containsAll(Storage.Blacklist.getBlacklist().getCommands()))
            Storage.Blacklist.getBlacklist().setList(packet.getCommands());

        if (Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED != packet.turnBlacklistToWhitelistEnabled())
            Storage.ConfigSections.Settings.TURN_BLACKLIST_TO_WHITELIST.ENABLED = packet.turnBlacklistToWhitelistEnabled();

        if(Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED != unknownCommandPacket.isEnabled())
            Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.ENABLED = unknownCommandPacket.isEnabled();

        Storage.ConfigSections.Settings.CUSTOM_UNKNOWN_COMMAND.MESSAGE = unknownCommandPacket.getMessage();

        if(Reflection.getMinor() >= 18) BukkitAntiTabListener.handleTabCompletion(Storage.Blacklist.getBlacklist().getCommands());
        if(!loaded) {
            loaded = true;
        }
    }

    public static void synchronizeGroupData(DataConverter.GroupsPacket packet) {
        GroupManager.clearAllGroups();
        packet.getGroups().forEach(group -> GroupManager.setGroup(group.getGroupName(), group.getCommands()));
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
