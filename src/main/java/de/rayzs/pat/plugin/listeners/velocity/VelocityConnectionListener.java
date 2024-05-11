package de.rayzs.pat.plugin.listeners.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.rayzs.pat.plugin.VelocityLoader;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;

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
        if(Storage.OUTDATED_VERSION && (PermissionUtil.hasPermission(player, "update"))) {
            server.getScheduler().buildTask(loader, () -> {
                if (player.isActive()) {
                    Storage.UPDATE_NOTIFICATION.forEach(message -> MessageTranslator.send(player, message));
                }
            }).delay(1, TimeUnit.SECONDS).schedule();
        }
    }
}
