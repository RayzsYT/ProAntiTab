package de.rayzs.pat.plugin;

import de.rayzs.pat.plugin.commands.BungeeCommand;
import de.rayzs.pat.plugin.listeners.bungee.BungeeAntiTabListener;
import de.rayzs.pat.plugin.listeners.bungee.BungeeBlockCommandListener;
import de.rayzs.pat.plugin.listeners.bungee.BungeePlayerConnectionListener;
import de.rayzs.pat.plugin.listeners.bungee.WaterfallAntiTabListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.brand.CustomServerBrand;
import de.rayzs.pat.utils.communication.ClientCommunication;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import de.rayzs.pat.utils.Storage;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import java.util.concurrent.TimeUnit;

public class BungeeLoader extends Plugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private ScheduledTask updaterTask;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\bungee-config.yml", "config.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\bungee-storage.yml", "storage.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getProxy());
        Storage.CURRENT_VERSION_NAME = getDescription().getVersion();

        Storage.load();
        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        GroupManager.initialize();
        bStats.initialize(this);
        PluginManager manager = ProxyServer.getInstance().getPluginManager();

        registerCommand("bungeeproantitab", "bpat", "pat", "proantitab");

        manager.registerListener(this, new BungeePlayerConnectionListener());
        manager.registerListener(this, new BungeeAntiTabListener());
        manager.registerListener(this, new BungeeBlockCommandListener());

        try {
            Class.forName("io.github.waterfallmc.waterfall.QueryResult");
            manager.registerListener(this, new WaterfallAntiTabListener());
        } catch (ClassNotFoundException ignored) {
            ProxyServer.getInstance().getScheduler().schedule(this, () -> {
                logger.warning("We advice you to use WaterFall instead for a better experience!");
                logger.warning("It's also required to really block every Bungeecord command with ProAntiTab!");
            }, 2, TimeUnit.SECONDS);
        }

        startUpdaterTask();
        ProxyServer.getInstance().getScheduler().schedule(this, () -> ClientCommunication.sendInformation("instance::"), 2, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        MessageTranslator.closeAudiences();
    }

    private static void registerCommand(String... commands) {
        for (String commandName : commands) {
            BungeeCommand command = new BungeeCommand(commandName);
            ProxyServer.getInstance().getPluginManager().registerCommand(plugin, command);
        }
    }

    public void startUpdaterTask() {
        if (!Storage.UPDATE_ENABLED) return;
        updaterTask = getProxy().getScheduler().schedule(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            Storage.NEWEST_VERSION_NAME = result;

            if (!Storage.NEWEST_VERSION_NAME.equals(Storage.CURRENT_VERSION_NAME)) {
                getProxy().getScheduler().cancel(updaterTask);
                if (result.equals("unknown")) {
                    Logger.warning("Failed reaching web host! (firewall enabled? website down?)");
                } else if (result.equals("exception")) {
                    Logger.warning("Failed creating web instance! (outdated java version?)");
                } else {
                    Storage.OUTDATED_VERSION = true;
                    Storage.UPDATE_NOTIFICATION.forEach(Logger::warning);
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
