package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.api.netty.proxy.VelocityPacketAnalyzer;
import de.rayzs.pat.plugin.logger.Logger;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.*;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.storage.Storage;
import com.velocitypowered.api.proxy.*;
import de.rayzs.pat.utils.sender.CommandSender;
import de.rayzs.pat.utils.sender.CommandSenderHandler;
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
        final CommandSender sender = CommandSenderHandler.from(player);

        sender.updateSenderObject(player);

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED) {
            final ServerInfo serverInfo = event.getOriginalServer().getServerInfo();
            if (serverInfo != null) Storage.tempCachePlayerToServer(player.getUniqueId(), serverInfo.getName());
        }

        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.JOINED);

        if(CustomServerBrand.isEnabled())
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive()) CustomServerBrand.sendBrandToPlayer(player);
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

            CommandSender sender = CommandSenderHandler.from(player);
            assert sender != null;
            PermissionUtil.reloadPermissions(sender);
        }

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) {
            return;
        }

        CustomServerBrand.sendBrandToPlayer(player);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.LEFT);

        PermissionUtil.resetPermissions(player.getUniqueId());
        VelocityPacketAnalyzer.uninject(player);
    }
}
