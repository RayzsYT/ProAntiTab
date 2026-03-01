package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.plugin.system.serverbrand.CustomServerBrand;
import de.rayzs.pat.api.event.PATEventHandler;
import de.rayzs.pat.api.event.events.ServerPlayersChangeEvent;
import de.rayzs.pat.plugin.packetanalyzer.proxy.BungeePacketAnalyzer;
import de.rayzs.pat.plugin.system.subargument.SubArgument;
import de.rayzs.pat.utils.permission.PermissionUtil;
import de.rayzs.pat.utils.message.MessageTranslator;
import de.rayzs.pat.utils.sender.CommandSender;
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
        final ProxiedPlayer player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);

        PATEventHandler.callServerPlayersChangeEvents(sender, ServerPlayersChangeEvent.Type.JOINED);
        SubArgument.get().updateCachedPlayerNames();

        if(CustomServerBrand.get().isEnabled())
            ProxyServer.getInstance().getScheduler().schedule(BungeeLoader.getPlugin(), () -> {
                if (player.isConnected()) CustomServerBrand.get().sendBrandToPlayer(player);
            }, 500, TimeUnit.MILLISECONDS);

        PermissionUtil.setPlayerPermissions(sender);

        if (Storage.OUTDATED && PermissionUtil.hasPermission(sender, "joinupdate")) {
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

        if (Storage.ConfigSections.Settings.UPDATE_GROUPS_PER_SERVER.ENABLED) {
            CommandSender sender = CommandSender.from(player);

            assert sender != null;
            PermissionUtil.reloadPermissions(sender);
        }

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) {
            return;
        }

        CustomServerBrand.get().sendBrandToPlayer(player);
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();
        final CommandSender sender = CommandSender.from(player);

        PATEventHandler.callServerPlayersChangeEvents(sender, ServerPlayersChangeEvent.Type.LEFT);
        SubArgument.get().updateCachedPlayerNames();

        PermissionUtil.resetPermissions(player.getUniqueId());
        BungeePacketAnalyzer.uninject(player);

        if (Storage.ConfigSections.Settings.CUSTOM_BRAND.REPEAT_DELAY != -1) {
            return;
        }

        CustomServerBrand.get().sendBrandToPlayer(player);
    }
}
