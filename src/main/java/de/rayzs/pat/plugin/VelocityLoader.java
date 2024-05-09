package de.rayzs.pat.plugin;

import com.google.inject.Inject;
import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.plugin.commands.VelocityCommand;
import de.rayzs.pat.plugin.listeners.velocity.VelocityAntiTabListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityBlockCommandListener;
import de.rayzs.pat.plugin.listeners.velocity.VelocityConnectionListener;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.metrics.impl.VelocityMetrics;
import de.rayzs.pat.utils.ConnectionBuilder;
import de.rayzs.pat.utils.MessageTranslator;
import de.rayzs.pat.utils.Reflection;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.utils.group.GroupManager;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.concurrent.TimeUnit;

@Plugin(name = "ProAntiTab",
id = "proantitab",
version = "1.5.3",
authors = "Rayzs_YT",
description = "A simple structured AntiTab plugin to prevent specific commands from being executed and auto-tab-completed.")
public class VelocityLoader {

    private final VelocityLoader plugin;
    private final ProxyServer server;
    private final EventManager manager;
    private final VelocityMetrics.Factory metricsFactory;
    private ScheduledTask task;

    @Inject
    public VelocityLoader(ProxyServer server, VelocityMetrics.Factory metricsFactory) {
        this.plugin = this;
        this.server = server;
        this.manager = server.getEventManager();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Reflection.initialize(server);
        Storage.load();
        GroupManager.initialize();

        metricsFactory.make(this, 21638);

        server.getCommandManager().register("pat", new VelocityCommand(), "bpat", "bungeeproantitab", "proantitab");
        server.getEventManager().register(this, new VelocityBlockCommandListener(server));
        server.getEventManager().register(this, new VelocityAntiTabListener(server));
        server.getEventManager().register(this, new VelocityConnectionListener(server, this));

        startUpdaterTask();
    }

    public void startUpdaterTask() {
        if (!Storage.UPDATE_ENABLED) return;

        task = server.getScheduler().buildTask(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            if (!result.equals("1.5.3")) {

                if (result.equals("unknown")) {
                    Logger.warning("Failed reaching web host! (firewall enabled? website down?)");
                } else if (result.equals("exception")) {
                    Logger.warning("Failed creating web instance! (outdated java version?)");
                } else {
                    Storage.OUTDATED_VERSION = true;
                    Storage.UPDATE_NOTIFICATION.forEach(message -> server.getConsoleCommandSource().sendMessage(MiniMessage.miniMessage().deserialize(MessageTranslator.translate(message.replace("&", "§")))));
                }
            }
        }).delay(1, TimeUnit.SECONDS).repeat(Storage.UPDATE_PERIOD, TimeUnit.SECONDS).schedule();
    }
}
