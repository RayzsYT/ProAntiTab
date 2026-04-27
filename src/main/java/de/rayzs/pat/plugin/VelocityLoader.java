package de.rayzs.pat.plugin;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.rayzs.pat.plugin.system.communication.cph.impl.VelocityCommunicationHandler;
import de.rayzs.pat.plugin.system.serverbrand.CustomServerBrand;
import de.rayzs.pat.plugin.system.communication.pmc.impl.VelocityPluginMessageClient;
import de.rayzs.pat.plugin.packetanalyzer.proxy.VelocityPacketAnalyzer;
import de.rayzs.pat.plugin.system.converter.StorageConverter;
import de.rayzs.pat.plugin.system.serverbrand.impl.VelocityServerBrand;
import de.rayzs.pat.plugin.command.CommandProcess;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.CommandsCache;
import de.rayzs.pat.plugin.metrics.impl.VelocityMetrics;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.plugin.command.impl.VelocityCommand;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.plugin.system.communication.Communicator;
import de.rayzs.pat.utils.hooks.LuckPermsHook;
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
import de.rayzs.pat.utils.response.ResponseHandler;
import de.rayzs.pat.utils.response.action.ActionHandler;
import de.rayzs.pat.utils.sender.CommandSender;

import java.util.*;

@Plugin(name = "ProAntiTab",
id = "proantitab",
version = "2.3.2",
authors = "Rayzs_YT",
description = "Hides more than just your plugins.",
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

        Reflection.initialize(server);

        CommandProcess.initialize();
        ConfigUpdater.initialize();

        Storage.initialize(this, pluginContainer.getDescription().getVersion().get());
        VersionComparer.get().setCurrentVersion(Storage.CURRENT_VERSION);

        Storage.loadAll(true);
        MessageTranslator.initialize();
        GroupManager.initialize();

        metricsFactory.make(this, 21638);

        server.getCommandManager().register("bpat", new VelocityCommand(), "bungeeproantitab");
        server.getEventManager().register(this, new VelocityBlockCommandListener());
        server.getEventManager().register(this, new VelocityAntiTabListener(server));
        server.getEventManager().register(this, new VelocityConnectionListener(server, this));
        server.getEventManager().register(this, new VelocityPingListener(server));

        startUpdaterTask();

        if (server.getPluginManager().getPlugin("luckperms").isPresent())
            LuckPermsHook.initialize();

        if (server.getPluginManager().getPlugin("papiproxybridge").isPresent()) {
            Storage.USE_PAPIPROXYBRIDGE = true;
            Logger.info("Successfully hooked into PAPIProxyBridge!");
        }

        Storage.broadcastPermissionsPluginNotice();
        ConfigUpdater.broadcastMissingParts();

        ActionHandler.initialize();
        SubArgument.initialize();

        ResponseHandler.update();

        StorageConverter.initialize();
        CustomServerBrand.initialize(new VelocityServerBrand());

        Communicator.initialize(new VelocityPluginMessageClient(), new VelocityCommunicationHandler());

        VelocityPacketAnalyzer.injectAll();
    }

    public static org.slf4j.Logger getPluginLogger() {
        return logger;
    }

    @Override
    public Object getPluginObj() {
        return instance;
    }

    @Override
    public void updateCommands() {
        Communicator.Proxy2Backend.sendUpdateCommand();
    }

    @Override
    public void updateCommands(CommandSender sender) {
        Communicator.Proxy2Backend.sendUpdateCommand(sender.getUniqueId(), sender.getServerName());
    }

    @Override
    public void delayedPermissionsReload() {
        server.getScheduler().buildTask(VelocityLoader.instance, () -> {
            PermissionUtil.reloadPermissions();
            resetCommandsCache();

            Communicator.Proxy2Backend.sendUpdateCommand();
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Override
    public void delayedPermissionsReload(CommandSender sender) {
        server.getScheduler().buildTask(VelocityLoader.instance, () -> {
            PermissionUtil.reloadPermissions(sender);

            Communicator.Proxy2Backend.sendUpdateCommand(sender.getUniqueId(), sender.getServerName());
        }).delay(1, TimeUnit.SECONDS).schedule();
    }

    @Override
    public void handleReload() {}

    @Override
    public boolean doesCommandExist(String command) {
        return false;
    }

    @Override
    public List<String> getAllCommands(boolean useColons) {
        return List.of();
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
    public void resetCommandsCache() {
        new ArrayList<>(commandsCacheMap.values()).forEach(CommandsCache::reset);
    }

    @Override
    public HashMap<String, CommandsCache> getPerServerCommandsCacheMap() {
        return commandsCacheMap;
    }

    @Override
    public CommandsCache getBukkitCommandsCacheMap() {
        return null;
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
        final String cachedServerName = Storage.getCachedPlayerServername(uuid);
        if (cachedServerName != null) {
            return cachedServerName;
        }

        Optional<Player> optPlayer = server.getPlayer(uuid);

        if (optPlayer.isEmpty())
            return null;

        Player player = optPlayer.get();
        Optional<ServerConnection> optConnection = player.getCurrentServer();

        if (optConnection.isEmpty()) {
            return null;
        }

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
    public List<String> getFormattedPluginNames(String format) {
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

    @Override
    public List<String> getPluginCommands(String pluginName, boolean useColons) {
        return List.of();
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
