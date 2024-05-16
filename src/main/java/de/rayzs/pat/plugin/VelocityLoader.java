package de.rayzs.pat.plugin;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.plugin.commands.VelocityCommand;
import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityBlockCommandListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityConnectionListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.impl.VelocityMetrics;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.plugin.brand.CustomServerBrand;
import de.rayzs.pat.plugin.communication.ClientCommunication;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.group.GroupManager;

import java.util.concurrent.TimeUnit;

@Plugin(name = "ProAntiTab",
id = "proantitab",
version = "1.6.0",
authors = "Rayzs_YT",
description = "A simple structured AntiTab plugin to prevent specific commands from being executed and auto-tab-completed.")
public class VelocityLoader {

    private static VelocityLoader instance;
    private static ProxyServer server;
    private final EventManager manager;
    private final VelocityMetrics.Factory metricsFactory;
    private ScheduledTask task;

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

        Configurator.createResourcedFile("./plugins/ProAntiTab", "files\\velocity-config.yml", "config.yml", false);
        Configurator.createResourcedFile("./plugins/ProAntiTab", "files\\velocity-storage.yml", "storage.yml", false);

        Reflection.initialize(server);
        Storage.CURRENT_VERSION_NAME = pluginContainer.getDescription().getVersion().get();

        Storage.load();
        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        GroupManager.initialize();


        metricsFactory.make(this, 21638);

        server.getCommandManager().register("pat", new VelocityCommand(), "bpat", "bungeeproantitab", "proantitab");
        server.getEventManager().register(this, new VelocityBlockCommandListener(server));
        server.getEventManager().register(this, new VelocityAntiTabListener(server));
        server.getEventManager().register(this, new VelocityConnectionListener(server, this));

        startUpdaterTask();
        server.getScheduler().buildTask(this, () -> {
            ClientCommunication.sendInformation("instance::");
            ClientCommunication.synchronizeInformation();
        }).delay(2, TimeUnit.SECONDS).schedule();
    }

    public void startUpdaterTask() {
        if (!Storage.UPDATE_ENABLED) return;

        task = server.getScheduler().buildTask(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();

            Storage.NEWEST_VERSION_NAME = result;
            if (!Storage.NEWEST_VERSION_NAME.equals(Storage.CURRENT_VERSION_NAME)) {

                if (Storage.NEWEST_VERSION_NAME.equals("unknown")) {
                    Logger.warning("Failed reaching web host! (firewall enabled? website down?)");
                } else if (result.equals("exception")) {
                    Logger.warning("Failed creating web instance! (outdated java version?)");
                } else {
                    Storage.OUTDATED_VERSION = true;
                    Storage.UPDATE_NOTIFICATION.forEach(message -> MessageTranslator.send(server.getConsoleCommandSource(), message));
                }
            }
        }).delay(1, TimeUnit.SECONDS).repeat(Storage.UPDATE_PERIOD, TimeUnit.SECONDS).schedule();
    }

    public static ProxyServer getServer() {
        return server;
    }

    public static VelocityLoader getInstance() {
        return instance;
    }
}
