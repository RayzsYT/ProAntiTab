package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.plugin.BungeeLoader;
import de.rayzs.pat.utils.PermissionUtil;
import de.rayzs.pat.utils.Storage;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.concurrent.TimeUnit;

public class BungeePlayerConnectionListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        if(Storage.OUTDATED_VERSION && (PermissionUtil.hasPermission(player, "update"))) {
            ProxyServer.getInstance().getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
                if (player.isConnected()) {
                    Storage.UPDATE_NOTIFICATION.forEach(message -> MessageTranslator.send(player, message));
                }
            }, 1, TimeUnit.SECONDS);
        }
    }
}
