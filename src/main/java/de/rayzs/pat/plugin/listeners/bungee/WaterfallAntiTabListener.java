package de.rayzs.pat.plugin.listeners.bungee;

import de.rayzs.pat.api.storage.Storage;
import de.rayzs.pat.utils.PermissionUtil;
import io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class WaterfallAntiTabListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onProxyDefineCommands(ProxyDefineCommandsEvent event) {
        if(!(event.getReceiver() instanceof ProxiedPlayer) || event.getCommands().isEmpty()) return;

        ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
        if(PermissionUtil.hasBypassPermission(player)) return;

        event.getCommands().entrySet().removeIf(entry -> Storage.BLACKLIST.isBlocked(player, entry.getKey()));
    }
}
