package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.brand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.api.netty.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.plugin.BungeeLoader;
import net.md_5.bungee.api.ProxyServer;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.event.*;

public class BungeePlayerConnectionListener implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.JOINED);

        if(CustomServerBrand.isEnabled())
            ProxyServer.getInstance().getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
                if (player.isConnected()) CustomServerBrand.sendBrandToPlayer(player);
            }, 500, TimeUnit.MILLISECONDS);

        PermissionUtil.setPlayerPermissions(player.getUniqueId());

        if(Storage.OUTDATED && PermissionUtil.hasPermission(player, "joinupdate")) {
            ProxyServer.getInstance().getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
                if (player.isConnected()) {
                    MessageTranslator.send(player, Storage.ConfigSections.Settings.UPDATE.OUTDATED, "%player%", player.getName());
                }
            }, 1, TimeUnit.SECONDS);
        }
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        ProxiedPlayer player = event.getPlayer();

        BungeePacketAnalyzer.inject(player);
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        CustomServerBrand.sendBrandToPlayer(player);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        PATEventHandler.callServerPlayersChangeEvents(player, ServerPlayersChangeEvent.Type.LEFT);

        PermissionUtil.resetPermissions(player.getUniqueId());
        BungeePacketAnalyzer.uninject(player);
        if(Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) return;
        CustomServerBrand.sendBrandToPlayer(player);
    }
}
