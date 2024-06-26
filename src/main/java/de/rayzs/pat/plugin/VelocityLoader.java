package de.rayzs.pat.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.commands.VelocityCommand;
import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityBlockCommandListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityConnectionListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityPingListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.impl.VelocityMetrics;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Plugin(name = "ProAntiTab",
id = "proantitab",
version = "1.7.5",
authors = "Rayzs_YT",
description = "Hide more then just plugins. Hide almost everything!",
dependencies = {
        @Dependency(id = "luckperms", optional = true),
        @Dependency(id = "papiproxybridge", optional = true)
})
public class VelocityLoader {

    private static VelocityLoader instance;
    private static ProxyServer server;
    private final EventManager manager;
    private final VelocityMetrics.Factory metricsFactory;
    private ScheduledTask task;
    private static boolean checkUpdate = false;

    @Inject
    public VelocityLoader(ProxyServer server, VelocityMetrics.Factory metricsFactory) {
        VelocityLoader.instance = this;
        VelocityLoader.server = server;
        this.manager = server.getEventManager();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PluginContainer pluginContainer = server.getPluginManager().getPlugin("proantitab").get();

        Configurator.createResourcedFile("./plugins/ProAntiTab", "files\\proxy-config.yml", "config.yml", false);
        Configurator.createResourcedFile("./plugins/ProAntiTab", "files\\proxy-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile("./plugins/ProAntiTab", "files\\proxy-placeholders.yml", "placeholders.yml", false);

        Reflection.initialize(server);
        ConfigUpdater.initialize();

        Storage.CURRENT_VERSION = pluginContainer.getDescription().getVersion().get();

        Storage.loadAll(true);
        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        GroupManager.initialize();


        metricsFactory.make(this, 21638);

        server.getCommandManager().register("bpat", new VelocityCommand(), "bungeeproantitab");
        server.getEventManager().register(this, new VelocityBlockCommandListener(server));
        server.getEventManager().register(this, new VelocityAntiTabListener(server));
        server.getEventManager().register(this, new VelocityConnectionListener(server, this));
        server.getEventManager().register(this, new VelocityPingListener(server));

        startUpdaterTask();
        server.getScheduler().buildTask(this, () -> {
            Communicator.syncData();
            Communicator.syncData();
        }).delay(2, TimeUnit.SECONDS).schedule();

        Storage.PLUGIN_OBJECT = this;

        if(server.getPluginManager().getPlugin("luckperms").isPresent())
            LuckPermsAdapter.initialize();

        if(server.getPluginManager().getPlugin("papiproxybridge").isPresent()) {
            Storage.USE_PAPIPROXYBRIDGE = true;
            Logger.info("Successfully hooked into PAPIProxyBridge!");
        }
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED) return;

        task = server.getScheduler().buildTask(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();

            Storage.NEWER_VERSION = result;
            if (!Storage.NEWER_VERSION.equals(Storage.CURRENT_VERSION)) {

                if (Storage.NEWER_VERSION.equals("unknown")) {
                    Logger.warning("Failed reaching web host! (firewall enabled? website down?)");
                } else if (result.equals("exception")) {
                    Logger.warning("Failed creating web instance! (outdated java version?)");
                } else {
                    Storage.OUTDATED = true;
                    MessageTranslator.send(server.getConsoleCommandSource(), Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines());
                }
            } else {
                if(!checkUpdate) {
                    checkUpdate = true;
                    MessageTranslator.send(server.getConsoleCommandSource(), Storage.ConfigSections.Settings.UPDATE.UPDATED.getLines());
                }
            }
        }).delay(1, TimeUnit.SECONDS).repeat(Storage.ConfigSections.Settings.UPDATE.PERIOD, TimeUnit.SECONDS).schedule();
    }

    public static ProxyServer getServer() {
        return server;
    }

    public static VelocityLoader getInstance() {
        return instance;
    }

    public static List<String> getServerNames() {
        List<String> servers = new ArrayList<>();
        server.getAllServers().forEach(server -> servers.add(server.getServerInfo().getName()));
        return servers;
    }
}
