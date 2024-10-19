package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.api.netty.proxy.VelocityPacketAnalyzer;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.api.brand.CustomServerBrand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.*;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.api.storage.Storage;
import com.velocitypowered.api.proxy.*;
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
        Player player = event.getPlayer();
        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.JOINED);

        if(CustomServerBrand.isEnabled())
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive()) CustomServerBrand.sendBrandToPlayer(player);
            }).delay(500, TimeUnit.MILLISECONDS).schedule();

        PermissionUtil.setPlayerPermissions(player.getUniqueId());

        if(Storage.OUTDATED && PermissionUtil.hasPermission(player, "joinupdate")) {
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive())
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED, "%player%", player.getUsername());
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();

        if(!VelocityPacketAnalyzer.isInjected(player))
            VelocityPacketAnalyzer.inject(player);

        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
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
