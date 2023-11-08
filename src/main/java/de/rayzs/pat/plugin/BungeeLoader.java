package de.rayzs.pat.plugin;

import de.rayzs.pat.plugin.commands.BungeeCommand;
import de.rayzs.pat.plugin.listeners.*;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.Configurator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import de.rayzs.pat.utils.Storage;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class BungeeLoader extends Plugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private ScheduledTask updaterTask;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\bungee-config.yml", "config.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getProxy());
        Storage.load();
        bStats.initialize(this);
        PluginManager manager = ProxyServer.getInstance().getPluginManager();

        Arrays.asList(new String[] { "bungeeproantitab", "bpat" } ).forEach(commandName -> {
            BungeeCommand command = new BungeeCommand(commandName);
            manager.registerCommand(this, command);
        });

        manager.registerListener(this, new BungeePlayerConnectionListener());
        manager.registerListener(this, new BungeeAntiTabListener());
        manager.registerListener(this, new BungeeBlockCommandListener());

        startUpdaterTask();
    }

    public void startUpdaterTask() {
        if (!Storage.UPDATE_ENABLED) return;
        updaterTask = getProxy().getScheduler().schedule(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            if (!result.equals(getDescription().getVersion())) {
                getProxy().getScheduler().cancel(updaterTask);
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
        }, 20L, Storage.UPDATE_PERIOD, TimeUnit.MILLISECONDS);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static java.util.logging.Logger getPluginLogger() {
        return logger;
    }
}
