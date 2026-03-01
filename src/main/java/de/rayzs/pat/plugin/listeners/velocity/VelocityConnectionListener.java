package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.rayzs.pat.plugin.system.serverbrand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.plugin.packetanalyzer.proxy.VelocityPacketAnalyzer;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.*;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.storage.Storage;
import com.velocitypowered.api.proxy.*;
import de.rayzs.pat.utils.sender.CommandSender;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public class VelocityConnectionListener {

    private final ProxyServer server;
    private final VelocityLoader loader;

    public VelocityConnectionListener(ProxyServer server, VelocityLoader loader) {
        this.server = server;
        this.loader = loader;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        final Player player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED) {
            final ServerInfo serverInfo = event.getOriginalServer().getServerInfo();
            if (serverInfo != null) Storage.tempCachePlayerToServer(player.getUniqueId(), serverInfo.getName());
        }

        PATEventHandler.callServerPlayersChangeEvents(sender, ServerPlayersChangeEvent.Type.JOINED);
        SubArgument.get().updateCachedPlayerNames();

        if(CustomServerBrand.get().isEnabled())
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive()) CustomServerBrand.get().sendBrandToPlayer(player);
            }).delay(500, TimeUnit.MILLISECONDS).schedule();

        PermissionUtil.setPlayerPermissions(sender);

        if (Storage.OUTDATED && PermissionUtil.hasPermission(sender, "joinupdate")) {
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive())
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED, "%player%", player.getUsername());
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        final Player player = event.getPlayer();

        if (!VelocityPacketAnalyzer.isInjected(player)) {

            if (!VelocityPacketAnalyzer.inject(player)) {
                server.getScheduler().buildTask(loader, () -> {
                    if (!VelocityPacketAnalyzer.inject(player)) {
                        if (Storage.ConfigSections.Settings.INJECTION_FAILED.ENABLED) {

                            Logger.info(MessageTranslator.replaceMessage(
                                    player,
                                    Storage.ConfigSections.Settings.INJECTION_FAILED.CONSOLE_MESSAGE.get()
                            ));

                            player.disconnect(Component.text(
                                    MessageTranslator.replaceMessage(
                                            player,
                                            Storage.ConfigSections.Settings.INJECTION_FAILED.KICK_MESSAGE.get()
                                    )
                            ));
                        }
                    }

                }).delay(1, TimeUnit.SECONDS).schedule();
            }
        }

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED) {
            final ServerInfo serverInfo = event.getServer().getServerInfo();
            if (serverInfo != null) Storage.tempCachePlayerToServer(player.getUniqueId(), serverInfo.getName());

            CommandSender sender = CommandSender.from(player);
            assert sender != null;
            PermissionUtil.reloadPermissions(sender);
        }

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) {
            return;
        }

        CustomServerBrand.get().sendBrandToPlayer(player);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        final Player player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);

        PATEventHandler.callServerPlayersChangeEvents(sender, ServerPlayersChangeEvent.Type.LEFT);
        SubArgument.get().updateCachedPlayerNames();

        PermissionUtil.resetPermissions(player.getUniqueId());
        VelocityPacketAnalyzer.uninject(player);
    }
}
