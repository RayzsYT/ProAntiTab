package de.rayzs.pat.plugin;

import de.rayzs.pat.api.netty.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.commands.BungeeCommand;
import de.rayzs.pat.plugin.listeners.bungee.*;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BungeeLoader extends Plugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private ScheduledTask updaterTask;
    private static boolean checkUpdate = false;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\proxy-config.yml", "config.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\proxy-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\placeholders.yml", "placeholders.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getProxy());
        Storage.CURRENT_VERSION = getDescription().getVersion();
        Storage.loadAll(true);

        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        GroupManager.initialize();
        bStats.initialize(this);
        PluginManager manager = ProxyServer.getInstance().getPluginManager();

        registerCommand("bungeeproantitab", "bpat");

        manager.registerListener(this, new BungeePlayerConnectionListener());
        manager.registerListener(this, new BungeeAntiTabListener());
        manager.registerListener(this, new BungeeBlockCommandListener());
        manager.registerListener(this, new BungeePingListener());

        if(Reflection.isPaper())
            manager.registerListener(this, new WaterfallAntiTabListener());

        startUpdaterTask();
        ProxyServer.getInstance().getScheduler().schedule(this, () -> {
            Communicator.syncData();
            Communicator.syncData();
        }, 5, TimeUnit.SECONDS);

        Storage.PLUGIN_OBJECT = this;

        if (manager.getPlugin("LuckPerms") != null)
            LuckPermsAdapter.initialize();

        if(manager.getPlugin("PAPIProxyBridge") != null) {
            Storage.USE_PAPIPROXYBRIDGE = true;
            Logger.info("Successfully hooked into PAPIProxyBridge!");
        }

        BungeePacketAnalyzer.injectAll();
    }

    @Override
    public void onDisable() {
        BungeePacketAnalyzer.uninjectAll();
        MessageTranslator.closeAudiences();
    }

    private static void registerCommand(String... commands) {
        for (String commandName : commands) {
            BungeeCommand command = new BungeeCommand(commandName);
            ProxyServer.getInstance().getPluginManager().registerCommand(plugin, command);
        }
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED) return;
        updaterTask = getProxy().getScheduler().schedule(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            Storage.NEWER_VERSION = result;

            if (!Storage.NEWER_VERSION.equals(Storage.CURRENT_VERSION)) {
                getProxy().getScheduler().cancel(updaterTask);
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
        }, 20L, Storage.ConfigSections.Settings.UPDATE.PERIOD, TimeUnit.MILLISECONDS);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static java.util.logging.Logger getPluginLogger() {
        return logger;
    }

    public static List<String> getServerNames() {
        return new ArrayList<>(ProxyServer.getInstance().getServers().keySet());
    }
}