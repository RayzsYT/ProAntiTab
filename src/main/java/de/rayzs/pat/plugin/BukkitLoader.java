package de.rayzs.pat.plugin;

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
    private static boolean loaded = false;
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
        Storage.CURRENT_VERSION_NAME = getDescription().getVersion();

        de.rayzs.pat.api.storage.Storage.loadAll(true);
        Storage.load(true);

        if(Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) MessageTranslator.enablePlaceholderSupport();
        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        bStats.initialize(this);

        PluginManager manager = getServer().getPluginManager();

        if(Storage.BUNGEECORD)
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ClientCommunication::sendRequest, 40, 20*10);
        else {
            loaded = true;
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
        if(!Storage.BUNGEECORD) PacketAnalyzer.uninjectAll();
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
        if (!Storage.UPDATE_ENABLED || Storage.BUNGEECORD) return;
        updaterTaskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            Storage.NEWEST_VERSION_NAME = result;

            if (!Storage.NEWEST_VERSION_NAME.equals(Storage.CURRENT_VERSION_NAME)) {
                Bukkit.getScheduler().cancelTask(updaterTaskId);
                if (result.equals("unknown")) {
                    Logger.warning("Failed reaching web host! (firewall enabled? website down?)");
                } else if (result.equals("exception")) {
                    Logger.warning("Failed creating web instance! (outdated java version?)");
                } else {
                    Storage.OUTDATED_VERSION = true;
                    Storage.UPDATE_NOTIFICATION.forEach(message -> Logger.warning(message.replace("&", "§")));
                }
            }
        }, 20L, 20L * Storage.UPDATE_PERIOD);
    }

    public static void synchronizeCommandData(DataConverter.CommandsPacket packet) {
        ClientCommunication.sendFeedback();

        if (!Storage.BLOCKED_COMMANDS_LIST.containsAll(packet.getCommands()) || !packet.getCommands().containsAll(Storage.BLOCKED_COMMANDS_LIST))
            Storage.BLOCKED_COMMANDS_LIST = packet.getCommands();

        if (Storage.TURN_BLACKLIST_TO_WHITELIST != packet.turnBlacklistToWhitelistEnabled())
            Storage.TURN_BLACKLIST_TO_WHITELIST = packet.turnBlacklistToWhitelistEnabled();

        if(Reflection.getMinor() >= 18) BukkitAntiTabListener.handleTabCompletion(packet.getCommands());
        if(!loaded) {
            loaded = true;
            Logger.info("First data has arrived successfully!");
        }
    }

    public static void synchronizeGroupData(DataConverter.GroupsPacket packet) {
        GroupManager.clearAllGroups();
        packet.getGroups().forEach(group -> GroupManager.setGroup(group.getGroupName(), group.getCommands()));
    }

    public static List<String> getNotBlockedCommands() {
        List<String> commands = new ArrayList<>();
        Bukkit.getHelpMap().getHelpTopics().stream()
                .filter(topic -> !topic.getName().contains(":") && topic.getName().startsWith("/") && !Storage.isBlocked(topic.getName().replaceFirst("/", ""), false))
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
