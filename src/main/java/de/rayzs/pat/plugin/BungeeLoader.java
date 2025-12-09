package de.rayzs.pat.plugin;


import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.plugin.converter.StorageConverter;
import de.rayzs.pat.plugin.process.CommandProcess;
import de.rayzs.pat.plugin.modules.SubArgsModule;
import de.rayzs.pat.api.netty.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.api.communication.Communicator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.response.action.ActionHandler;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import de.rayzs.pat.plugin.commands.BungeeCommand;
import de.rayzs.pat.plugin.listeners.bungee.*;
import de.rayzs.pat.utils.group.GroupManager;
import net.md_5.bungee.api.config.ServerInfo;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.plugin.logger.Logger;
import net.md_5.bungee.api.connection.*;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.ProxyServer;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.plugin.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class BungeeLoader extends Plugin implements PluginLoader {

    private ScheduledTask updaterTask;

    private static Plugin plugin;
    private static java.util.logging.Logger logger;

    private static final HashMap<String, CommandsCache> commandsCacheMap = new HashMap<>();

    @Override
    public void onLoad() {
        Configurator.createResourcedFile("files\\proxy-config.yml", "config.yml", false);
        Configurator.createResourcedFile("files\\proxy-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile("files\\proxy-placeholders.yml", "placeholders.yml", false);
        Configurator.createResourcedFile("files\\proxy-custom-responses.yml", "custom-responses.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        CommandProcess.initialize();
        Reflection.initialize(getProxy());
        ConfigUpdater.initialize();

        Storage.USE_SIMPLECLOUD = Reflection.doesClassExist("eu.thesimplecloud.plugin.startup.CloudPlugin");

        Storage.initialize(this, getDescription().getVersion());
        VersionComparer.get().setCurrentVersion(Storage.CURRENT_VERSION);

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

        if (Reflection.isPaper())
            manager.registerListener(this, new WaterfallAntiTabListener());

        startUpdaterTask();

        if (!Storage.ConfigSections.Settings.DISABLE_SYNC.DISABLED)
            ProxyServer.getInstance().getScheduler().schedule(this, Communicator::syncData, 5, TimeUnit.SECONDS);

        Storage.PLUGIN_OBJECT = this;

        if (manager.getPlugin("LuckPerms") != null)
            LuckPermsAdapter.initialize();

        if(manager.getPlugin("PAPIProxyBridge") != null) {
            Storage.USE_PAPIPROXYBRIDGE = true;
            Logger.info("Successfully hooked into PAPIProxyBridge!");
        }

        if(Storage.USE_SIMPLECLOUD)
            Logger.warning("Detected SimpleCloud and therefore MiniMessages by Kyori are disabled!");

        BungeePacketAnalyzer.injectAll();

        Storage.broadcastPermissionsPluginNotice();
        ConfigUpdater.broadcastMissingParts();

        ActionHandler.initialize();
        SubArgsModule.initialize();

        StorageConverter.initialize();

        // Reload proxy commands after 1, 5, and 15 seconds.
        for (int i : new Integer[] { 1, 5, 15 }) {
            ProxyServer.getInstance().getScheduler().schedule(this, BungeePacketAnalyzer::loadProxyCommands, i, TimeUnit.SECONDS);
        }
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

    @Override
    public void handleReload() {
        BungeePacketAnalyzer.loadProxyCommands();
    }

    @Override
    public void delayedPermissionsReload() {
        getProxy().getScheduler().schedule(this, () -> {
            PermissionUtil.reloadPermissions();
            Storage.getLoader().updateCommandCache();

            Communicator.sendRequest();
        }, 1, TimeUnit.SECONDS);
    }

    @Override
    public boolean doesCommandExist(String command) {
        return false;
    }

    @Override
    public Object getConsoleSender() {
        return ProxyServer.getInstance().getConsole();
    }

    @Override
    public Object getPlayerObjByName(String name) {
        return plugin.getProxy().getPlayer(name);
    }

    @Override
    public Object getPlayerObjByUUID(UUID uuid) {
        return plugin.getProxy().getPlayer(uuid);
    }

    @Override
    public HashMap<String, CommandsCache> getCommandsCacheMap() {
        return commandsCacheMap;
    }

    @Override
    public void updateCommandCache() {
        new ArrayList<>(commandsCacheMap.values()).forEach(CommandsCache::reset);
    }

    @Override
    public String getPlayerServerName(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);

        if (player == null || player.getServer() == null || player.getServer().getInfo() == null)
            return null;

        return player.getServer().getInfo().getName();
    }

    @Override
    public List<UUID> getPlayerIds() {
        return new ArrayList<>(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getUniqueId).toList());
    }

    @Override
    public List<UUID> getPlayerIdsByServer(String server) {
        List<UUID> uuids = new ArrayList<>();

        getServerNames().stream()
                .filter(serverName -> Storage.isServer(server, serverName))
                .forEach(serverName -> {
                    ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);

                    if (serverInfo == null)
                        return;

                    uuids.addAll(serverInfo.getPlayers().stream().map(ProxiedPlayer::getUniqueId).toList());
                });

        return uuids;
    }

    @Override
    public List<String> getOnlinePlayerNames(String serverName) {
        return new ArrayList<>(ProxyServer.getInstance().getPlayers().stream()
                .filter(player -> {
                    if (player == null || player.getServer() == null || player.getServer().getInfo() == null)
                        return false;

                    return player.getServer().getInfo().getName().equalsIgnoreCase(serverName);

                }).map(ProxiedPlayer::getName).toList());
    }

    @Override
    public boolean isPlayerOnline(String playerName) {
        return ProxyServer.getInstance().getPlayer(playerName) != null;
    }

    @Override
    public boolean doesPlayerExist(String playerName) {
        return isPlayerOnline(playerName);
    }

    @Override
    public List<String> getOnlinePlayerNames() {
        return new ArrayList<>(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).toList());
    }

    @Override
    public List<String> getOfflinePlayerNames() {
        return new ArrayList<>(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).toList());
    }

    @Override
    public List<String> getPlayerNames() {
        return new ArrayList<>(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).toList());
    }

    @Override
    public String getNameByUUID(UUID uuid) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        return player != null ? player.getName() : "";
    }

    @Override
    public UUID getUUIDByName(String playerName) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerName);
        return player != null ? player.getUniqueId() : null;
    }

    @Override
    public List<String> getServerNames() {
        return new ArrayList<>(ProxyServer.getInstance().getServers().keySet().stream().toList());
    }

    @Override
    public List<String> getPluginNames(String format) {
        List<String> pluginNames = new ArrayList<>();

        for (Plugin plugin : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            PluginDescription description = plugin.getDescription();

            pluginNames.add(
                    format.replace("%n", description.getName())
                            .replace("%v", description.getVersion())
            );
        }

        return pluginNames;
    }

    @Override
    public List<String> getPluginCommands(String pluginName, boolean useColons) {
        return List.of();
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED)
            return;

        updaterTask = getProxy().getScheduler().schedule(this, () -> {

            if (VersionComparer.get().computeComparison())
                getProxy().getScheduler().cancel(updaterTask);

        }, 20L, Storage.ConfigSections.Settings.UPDATE.PERIOD, TimeUnit.MILLISECONDS);
    }

    public static String getServerNameByPlayerUUID(UUID uuid) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(uuid);
        if(proxiedPlayer == null) return null;

        Server server = proxiedPlayer.getServer();
        if(server == null) return null;

        ServerInfo serverInfo = server.getInfo();
        if(serverInfo == null) return null;

        return serverInfo.getName();
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static java.util.logging.Logger getPluginLogger() {
        return logger;
    }
}