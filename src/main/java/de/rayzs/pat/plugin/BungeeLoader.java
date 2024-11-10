package de.rayzs.pat.plugin;

import de.rayzs.pat.plugin.modules.subargs.SubArgsModule;
import de.rayzs.pat.utils.configuration.updater.ConfigUpdater;
import de.rayzs.pat.api.netty.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.utils.configuration.Configurator;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.adapter.LuckPermsAdapter;
import de.rayzs.pat.api.communication.Communicator;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.*;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import de.rayzs.pat.plugin.commands.BungeeCommand;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.plugin.listeners.bungee.*;
import de.rayzs.pat.utils.group.GroupManager;
import de.rayzs.pat.plugin.metrics.bStats;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.api.storage.Storage;
import net.md_5.bungee.api.ProxyServer;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.plugin.*;
import de.rayzs.pat.utils.*;
import java.util.*;

public class BungeeLoader extends Plugin {

    private static Plugin plugin;
    private static java.util.logging.Logger logger;
    private ScheduledTask updaterTask;
    private static boolean checkUpdate = false;

    @Override
    public void onLoad() {
        Configurator.createResourcedFile(getDataFolder(), "files\\proxy-config.yml", "config.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\proxy-storage.yml", "storage.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\proxy-placeholders.yml", "placeholders.yml", false);
        Configurator.createResourcedFile(getDataFolder(), "files\\proxy-custom-responses.yml", "custom-responses.yml", false);
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = getLogger();

        Reflection.initialize(getProxy());
        ConfigUpdater.initialize();

        Storage.USE_SIMPLECLOUD = Reflection.doesClassExist("eu.thesimplecloud.plugin.startup.CloudPlugin");
        Storage.CURRENT_VERSION = getDescription().getVersion();
        VersionComparer.setCurrentVersion(Storage.CURRENT_VERSION);

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
        ConfigUpdater.broadcastMissingParts();
        SubArgsModule.initialize();
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

    public static List<String> getPlayerNames() {
        List<String> result = new LinkedList<>();
        ProxyServer.getInstance().getPlayers().forEach(player -> result.add(player.getName()));
        return result;
    }

    public static UUID getUUIDByName(String playerName) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(playerName);
        return proxiedPlayer != null ? proxiedPlayer.getUniqueId() : null;
    }

    public void startUpdaterTask() {
        if (!Storage.ConfigSections.Settings.UPDATE.ENABLED) return;
        updaterTask = getProxy().getScheduler().schedule(this, () -> {
            String result = new ConnectionBuilder().setUrl("https://www.rayzs.de/proantitab/api/version.php")
                    .setProperties("ProAntiTab", "4654").connect().getResponse();
            Storage.NEWER_VERSION = result;
            VersionComparer.setNewestVersion(Storage.NEWER_VERSION);

            if(VersionComparer.isDeveloperVersion()) {
                getProxy().getScheduler().cancel(updaterTask);
                Logger.info("§8[§fPAT | Proxy§8] §7Please be aware that you are currently using a §bdeveloper §7version of ProAntiTab. Bugs, errors and a lot of debug messages might be included.");

            } else if(!checkUpdate && (VersionComparer.isNewest() || VersionComparer.isUnreleased())) {
                getProxy().getScheduler().cancel(updaterTask);
                checkUpdate = true;

                if(VersionComparer.isUnreleased()) {
                    Logger.info("§8[§fPAT | Proxy§8] §7Please be aware that you are currently using an §eunreleased §7version of ProAntiTab.");
                    return;
                }

                checkUpdate = true;
                Storage.ConfigSections.Settings.UPDATE.UPDATED.getLines().forEach(Logger::warning);

            } else if(VersionComparer.isOutdated()) {
                Storage.OUTDATED = true;
                Storage.ConfigSections.Settings.UPDATE.OUTDATED.getLines().forEach(Logger::warning);

            } else if(!Storage.NEWER_VERSION.equals(Storage.CURRENT_VERSION)) {
                getProxy().getScheduler().cancel(updaterTask);
                switch (result) {
                    case "internet":
                        Logger.warning("Failed to build connection to website! (No internet?)");
                        break;
                    case "unknown":
                        Logger.warning("Failed to build connection to website! (Firewall enabled or website down?)");
                        break;
                    case "exception":
                        Logger.warning("Failed to build connection to website! (Outdated java version?)");
                        break;
                }
            }
        }, 20L, Storage.ConfigSections.Settings.UPDATE.PERIOD, TimeUnit.MILLISECONDS);
    }

    public static void sendTitle(UUID uuid, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if(player == null) return;
        Title titleObj = ProxyServer.getInstance().createTitle();
        titleObj.title(TextComponent.fromLegacyText(StringUtils.replace(title, "&", "§", "%player%", player.getName())));
        titleObj.subTitle(TextComponent.fromLegacyText(StringUtils.replace(subTitle, "&", "§", "%player%", player.getName())));
        titleObj.fadeIn(fadeIn);
        titleObj.stay(stay);
        titleObj.fadeOut(fadeOut);
        player.sendTitle(titleObj);
    }

    public static void executeConsoleCommand(UUID uuid, String command) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
        if(player != null) command = command.replace("%player%", player.getName());
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
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

    public static List<String> getServerNames() {
        List<String> servers = new LinkedList<>();
        for (String serverName : ProxyServer.getInstance().getServers().keySet())
            servers.add(serverName.toLowerCase());

        return servers;
    }
}