package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.api.brand.impl.VelocityServerBrand;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.permission.PermissionUtil;
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
        PermissionUtil.setPlayerPermissions(player.getUniqueId());

        if(Storage.OUTDATED && (PermissionUtil.hasPermission(player, "update"))) {
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive())
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED, "%player%", player.getUsername());
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        VelocityServerBrand.removeFromModified(player);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        PermissionUtil.resetPermissions(player.getUniqueId());
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        VelocityServerBrand.removeFromModified(player);
    }
}
