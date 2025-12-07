package de.rayzs.pat.plugin;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.plugin.converter.StorageConverter;
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.plugin.process.CommandProcess;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.plugin.metrics.impl.VelocityMetrics;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.commands.VelocityCommand;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.plugin.listeners.velocity.*;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.utils.VersionComparer;
import de.rayzs.pat.plugin.logger.Logger;
import com.velocitypowered.api.plugin.*;
import de.rayzs.pat.api.storage.Storage;
import com.velocitypowered.api.event.*;
import de.rayzs.pat.utils.Reflection;
import java.util.concurrent.TimeUnit;


import com.google.inject.Inject;
import de.rayzs.pat.utils.response.action.ActionHandler;

import java.util.*;

@Plugin(name = "ProAntiTab",
id = "proantitab",
version = "2.2.0",
authors = "Rayzs_YT",
description = "Hide more than just your plugins. Hide almost everything!",
url = "https://www.rayzs.de/products/proantitab/page",
dependencies = {
        @Dependency(id = "luckperms", optional = true),
        @Dependency(id = "papiproxybridge", optional = true)
})
public class VelocityLoader implements PluginLoader {

    private static VelocityLoader instance;

    private static ProxyServer server;
    private static org.slf4j.Logger logger;

    private final EventManager manager;
    private final VelocityMetrics.Factory metricsFactory;
    private ScheduledTask updaterTask;

    private static final HashMap<String, CommandsCache> commandsCacheMap = new HashMap<>();

    @Inject
    public VelocityLoader(ProxyServer server, org.slf4j.Logger logger, VelocityMetrics.Factory metricsFactory) {
        VelocityLoader.instance = this;
        VelocityLoader.server = server;
        VelocityLoader.logger = logger;

        this.manager = server.getEventManager();
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PluginContainer pluginContainer = server.getPluginManager().getPlugin("proantitab").get();

        Configurator.createResourcedFile("files\\proxy-config.yml", "config.yml", false);
        Configurator.createResourcedFile("files\\proxy-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile("files\\proxy-placeholders.yml", "placeholders.yml", false);
        Configurator.createResourcedFile("files\\proxy-custom-responses.yml", "custom-responses.yml", false);

        CommandProcess.initialize();
        Reflection.initialize(server);
        ConfigUpdater.initialize();

        Storage.USE_SIMPLECLOUD = Reflection.doesClassExist("eu.thesimplecloud.plugin.startup.CloudPlugin");
        Storage.initialize(this, pluginContainer.getDescription().getVersion().get());
        VersionComparer.get().setCurrentVersion(Storage.CURRENT_VERSION);

        Storage.loadAll(true);
        MessageTranslator.initialize();
        CustomServerBrand.initialize();
        GroupManager.initialize();

        metricsFactory.make(this, 21638);

        server.getCommandManager().register("bpat", new VelocityCommand(), "bungeeproantitab");
        server.getEventManager().register(this, new VelocityBlockCommandListener());
        server.getEventManager().register(this, new VelocityAntiTabListener(server));
        server.getEventManager().register(this, new VelocityConnectionListener(server, this));
        server.getEventManager().register(this, new VelocityPingListener(server));

        startUpdaterTask();

        if (!Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED)
            server.getScheduler().buildTask(this, Communicator::syncData).delay(5, TimeUnit.SECONDS).schedule();

        Storage.PLUGIN_OBJECT = this;

        if (server.getPluginManager().getPlugin("luckperms").isPresent())
            LuckPermsAdapter.initialize();

        if (server.getPluginManager().getPlugin("papiproxybridge").isPresent()) {
            Storage.USE_PAPIPROXYBRIDGE = true;
            Logger.info("Successfully hooked into PAPIProxyBridge!");
        }

        if(Storage.USE_SIMPLECLOUD)
            Logger.warning("Detected SimpleCloud and therefore MiniMessages by Kyori are disabled!");

        Storage.broadcastPermissionsPluginNotice();
        ConfigUpdater.broadcastMissingParts();

        ActionHandler.initialize();
        SubArgsModule.initialize();

        StorageConverter.initialize();
    }

    public static org.slf4j.Logger getPluginLogger() {
        return logger;
    }

    @Override
    public void delayedPermissionsReload() {
        server.getScheduler().buildTask(VelocityLoader.instance, () -> {
            PermissionUtil.reloadPermissions();
            Storage.getLoader().updateCommandCache();
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    public static void delayedPlayerReload(UUID uuid) {
        server.getScheduler().buildTask(VelocityLoader.instance, () -> {
            String serverName = Storage.getLoader().getPlayerServerName(uuid);
            List<String> commands = new ArrayList<>(SubArgsModule.getServerCommands(serverName));

            commands.addAll(SubArgsModule.getGroupCommands(uuid, serverName));

            PATEventHandler.callUpdatePlayerCommandsEvents(uuid, commands, true);
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Override
    public void handleReload() {}

    @Override
    public boolean doesCommandExist(String command) {
        return false;
    }

    @Override
    public Object getConsoleSender() {
        return server.getConsoleCommandSource();
    }

    @Override
    public Object getPlayerObjByName(String name) {
        return server.getPlayer(name).orElse(null);
    }

    @Override
    public Object getPlayerObjByUUID(UUID uuid) {
        return server.getPlayer(uuid).orElse(null);
    }

    @Override
    public void updateCommandCache() {
        new ArrayList<>(commandsCacheMap.values()).forEach(CommandsCache::reset);
    }

    @Override
    public HashMap<String, CommandsCache> getCommandsCacheMap() {
        return commandsCacheMap;
    }

    @Override
    public boolean isPlayerOnline(String playerName) {
        return server.getPlayer(playerName).isPresent();
    }

    @Override
    public boolean doesPlayerExist(String playerName) {
        return isPlayerOnline(playerName);
    }

    @Override
    public List<UUID> getPlayerIdsByServer(String serverName) {
        List<UUID> uuids = new ArrayList<>();

        getServerNames().stream()
                .filter(originServerName -> Storage.isServer(serverName, originServerName))
                .forEach(originServerName -> {
                    Optional<RegisteredServer> optionalRegServerInfo = server.getServer(serverName);

                    if (optionalRegServerInfo.isEmpty())
                        return;

                    RegisteredServer regServerInfo = optionalRegServerInfo.get();
                    uuids.addAll(regServerInfo.getPlayersConnected().stream().map(Player::getUniqueId).toList());
                });

        return uuids;
    }

    @Override
    public String getPlayerServerName(UUID uuid) {
        Optional<Player> optPlayer = server.getPlayer(uuid);

        if (optPlayer.isEmpty())
            return null;

        Player player = optPlayer.get();
        Optional<ServerConnection> optConnection = player.getCurrentServer();

        if (optConnection.isEmpty())
            return null;

        ServerConnection connection = optConnection.get();
        return connection.getServer().getServerInfo().getName();
    }

    @Override
    public List<UUID> getPlayerIds() {
        return server.getAllPlayers().stream().map(Player::getUniqueId).toList();
    }

    @Override
    public List<String> getOnlinePlayerNames(String serverName) {
        return server.getAllPlayers().stream()
                .filter(player -> {
                    Optional<ServerConnection> optConnection = player.getCurrentServer();

                    if (optConnection.isEmpty())
                        return false;

                    ServerConnection connection = optConnection.get();
                    return connection.getServer().getServerInfo().getName().equalsIgnoreCase(serverName);

                }).map(Player::getUsername).toList();
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return server.getAllPlayers().stream().map(Player::getUsername).toList();
    }

    @Override
    public List<String> getOfflinePlayerNames() {
        return server.getAllPlayers().stream().map(Player::getUsername).toList();
    }

    public List<String> getPlayerNames() {
        return server.getAllPlayers().stream().map(Player::getUsername).toList();
    }

    @Override
    public String getNameByUUID(UUID uuid) {
        Player player = server.getPlayer(uuid).orElse(null);
        return player != null ? player.getUsername() : "";
    }

    @Override
    public UUID getUUIDByName(String playerName) {
        Player player = server.getPlayer(playerName).orElse(null);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public List<String> getServerNames() {
        return new ArrayList<>(server.getAllServers().stream().map(registeredServer -> registeredServer.getServerInfo().getName()).toList());
    }

    @Override
    public List<String> getPluginNames(String format) {
        List<String> pluginNames = new ArrayList<>();
        for (PluginContainer plugin : server.getPluginManager().getPlugins()) {
            PluginDescription description = plugin.getDescription();

            String pluginName = description.getName().orElse("/");
            String version = description.getVersion().orElse("/");

            pluginNames.add(
                    format.replace("%n", pluginName)
                            .replace("%v", version)
            );
        }

        return pluginNames;
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED) return;

        updaterTask = server.getScheduler().buildTask(this, () -> {

            if (VersionComparer.get().computeComparison())
                updaterTask.cancel();

        }).delay(1, TimeUnit.SECONDS).repeat(Storage.ConfigSections.Settings.UPDATE.PERIOD, TimeUnit.SECONDS).schedule();
    }

    public static ProxyServer getServer() {
        return server;
    }

    public static VelocityLoader getInstance() {
        return instance;
    }
}
