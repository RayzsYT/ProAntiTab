package de.rayzs.pat.plugin;

import de.rayzs.pat.plugin.commands.BukkitCommand;
import de.rayzs.pat.plugin.listeners.BukkitAntiTabListener;
import de.rayzs.pat.plugin.listeners.BukkitBlockCommandListener;
import de.rayzs.pat.plugin.listeners.BukkitPlayerConnectionListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.plugin.netty.PacketAnalyzer;
import de.rayzs.pat.utils.configuration.Configurator;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import de.rayzs.pat.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BukkitLoader extends JavaPlugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private int updaterTaskId;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\bukkit-config.yml", "config.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getServer());
        Storage.load();
        bStats.initialize(this);

        PacketAnalyzer.injectAll();

        BukkitCommand command = new BukkitCommand();
        Arrays.asList(new String[] { "proantitab", "pat" } ).forEach(commandName -> {
            PluginCommand pluginCommand = getCommand(commandName);
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        });

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new BukkitPlayerConnectionListener(), this);
        manager.registerEvents(new BukkitBlockCommandListener(), this);
        if(Reflection.getMinor() >= 18) manager.registerEvents(new BukkitAntiTabListener(), this);

        startUpdaterTask();
    }

    @Override
    public void onDisable() {
        PacketAnalyzer.uninjectAll();
    }

    public void startUpdaterTask() {
        if (!Storage.UPDATE_ENABLED) return;
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
                    Logger.warning("You're using an outdated version of this plugin!");
                    Logger.warning("Please update it on: https://www.rayzs.de/products/proantitab/page");
                }
            }
        }, 20L, 20L * Storage.UPDATE_PERIOD);
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
