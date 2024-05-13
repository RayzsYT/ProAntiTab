package de.rayzs.pat.plugin;

import de.rayzs.pat.plugin.commands.BukkitCommand;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitAntiTabListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitBlockCommandListener;
import de.rayzs.pat.plugin.listeners.bukkit.BukkitPlayerConnectionListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.plugin.netty.PacketAnalyzer;
import de.rayzs.pat.utils.communication.ClientCommunication;
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

        Storage.CURRENT_VERSION_NAME = getDescription().getVersion();
        Reflection.initialize(getServer());

        Storage.load();
        MessageTranslator.initialize();
        bStats.initialize(this);

        PluginManager manager = getServer().getPluginManager();

        if(Storage.BUNGEECORD)
            Bukkit.getScheduler().scheduleSyncRepeatingTask(this, ClientCommunication::sendRequest, 40, 20*10);
        else {
            GroupManager.initialize();
            PacketAnalyzer.injectAll();

            manager.registerEvents(new BukkitBlockCommandListener(), this);
        }

        if(Reflection.getMinor() >= 18) manager.registerEvents(new BukkitAntiTabListener(), this);
        manager.registerEvents(new BukkitPlayerConnectionListener(), this);

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

    public static void synchronizeCommandData(boolean turn, List<String> commands) {
        if (!Storage.BLOCKED_COMMANDS_LIST.containsAll(commands) || !commands.containsAll(Storage.BLOCKED_COMMANDS_LIST))
            Storage.BLOCKED_COMMANDS_LIST = commands;

        if (Storage.TURN_BLACKLIST_TO_WHITELIST != turn)
            Storage.TURN_BLACKLIST_TO_WHITELIST = turn;

        if(Reflection.getMinor() >= 18) BukkitAntiTabListener.handleTabCompletion(commands);
    }

    public void startUpdaterTask() {
        if (!Storage.UPDATE_ENABLED || Storage.BUNGEECORD) return;
        updaterTaskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            if (!result.equals(getDescription().getVersion())) {
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

    public static void synchronizeGroupData(String information) {
        GroupManager.clearAllGroups();

        Map<String, List<String>> groups = new HashMap<>();
        String[] groupPartSplits = information.split("\\\\"),
                groupInformation;

        for (String groupPartSplit : groupPartSplits) {
            groupInformation = groupPartSplit.split(";");
            String groupName = groupInformation[0];
            List<String> commands = new ArrayList<>();
            for(int i = 1; i < groupInformation.length; i++)
                commands.add(groupInformation[i]);
            groups.put(groupName, commands);
        }

        for (Map.Entry<String, List<String>> entry : groups.entrySet())
            GroupManager.setGroup(entry.getKey(), entry.getValue());
    }

    public static List<String> getNotBlockedCommands() {
        List<String> commands = new ArrayList<>();
        Bukkit.getHelpMap().getHelpTopics().stream()
                .filter(topic -> !topic.getName().contains(":") && topic.getName().startsWith("/") && !Storage.isCommandBlocked(topic.getName().replaceFirst("/", "")))
                .forEach(topic -> commands.add(topic.getName().replaceFirst("/", "")));
        return commands;
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static java.util.logging.Logger getPluginLogger() {
        return logger;
    }
}
